package com.example.taskmanager.controller.User;

import com.example.taskmanager.domain.User.AccountSettings;
import com.example.taskmanager.domain.User.MyUser;
import com.example.taskmanager.repo.User.UserRepository;
import com.example.taskmanager.service.*;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.servlet.view.RedirectView;

import java.security.Principal;
import java.util.ArrayList;

@Controller
@RequestMapping("/social")
public class SocialController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private MembershipService membershipService;

    /**
     * Displays a users profile
     * @param model
     * @param principal
     * @return
     */
    @GetMapping("/my-profile")
    public String myProfile(Model model, Principal principal){
        MyUser user = userRepo.findByEmail(principal.getName());
        model.addAttribute("userProfile",user);
        model.addAttribute("user",user);
        model.addAttribute("settings",user.getAccountSettings());
        model.addAttribute("ownProfile",true);
        model.addAttribute("isUserFriend", false);
        model.addAttribute("sentFriendRequest", false);
        model.addAttribute("receivedFriendRequest",false);
        model.addAttribute("friendRequest",null);
        model.addAttribute("friends",user.getFriends());
        model.addAttribute("friendRequests",user.getReceivedFriendRequests());
        model.addAttribute("sentRequests",user.getSentFriendRequests());
        model.addAttribute("recentTasks", taskService.getTop6RecentlyCompletedTasks(user));
        model.addAttribute("recentAssignmentTasks", assignmentService.getTop6RecentlyCompletedTasks(user));
        return "social/profile-page";
    }

    /**
     * Handles the view of a users profile and checks if the user viewing profile is the user or if they are the users friend
     * which is used to handle user privacy detials
     * @param username
     * @param model
     * @param principal
     * @return
     */
    @GetMapping("/profile/{username}")
    public String viewUser(@PathVariable String username, Model model, Principal principal){
        MyUser userProfile = userRepo.findByUsername(username);
        MyUser user = userRepo.findByEmail(principal.getName());
        model.addAttribute("userProfile",userProfile);
        model.addAttribute("settings", userProfile.getAccountSettings());
        model.addAttribute("user", user);
        model.addAttribute("ownProfile",userProfile == user);
        model.addAttribute("isUserFriend", userProfile.getFriends().contains(user));
        model.addAttribute("sentFriendRequest", userService.hasUserSentFriendRequestToUserOrReceived(user,userProfile));
        model.addAttribute("receivedFriendRequest",userService.hasUserSentFriendRequestToUserOrReceived(userProfile,user));
        model.addAttribute("friendRequest",userService.getFriendRequestIfExists(userProfile,user));
        model.addAttribute("friends",userProfile.getFriends());
        model.addAttribute("friendRequests",new ArrayList<>());
        model.addAttribute("sentRequests",new ArrayList<>());
        model.addAttribute("recentTasks", taskService.getTop6RecentlyCompletedTasks(userProfile));
        model.addAttribute("recentAssignmentTasks", assignmentService.getTop6RecentlyCompletedTasks(userProfile));
        return "social/profile-page";
    }

    @GetMapping("/search")
    public String search(Model model, Principal principal){
        MyUser user = userService.getUserByEmail(principal.getName());
        model.addAttribute("user", user);
        return "social/search";
    }

    /**
     * Hnaldes user search query results which are displayed
     * @param search
     * @param model
     * @param principal
     * @return
     */
    @RequestMapping("/search")
    public String userSearchResults(@RequestParam(value = "search") String search, Model model, Principal principal){
        MyUser user = userService.getUserByEmail(principal.getName());
        model.addAttribute("user", user);
        model.addAttribute("results",userService.findAllMatchingSearch(search));
        model.addAttribute("search",search);
        return "social/search";
    }

    /**
     * Displays page containg all the users chats
     * @param model
     * @param principal
     * @return
     */
    @GetMapping("/messages")
    public String userChat(Model model, Principal principal){
        MyUser user = userService.getUserByEmail(principal.getName());
        model.addAttribute("user", user);
        model.addAttribute("chats", chatService.getUserChats(principal));
        return "social/messages";
    }

    @GetMapping("/account-settings")
    public String userSettings(Model model, Principal principal){
        MyUser user = userService.getUserByEmail(principal.getName());
        model.addAttribute("user",user);
        model.addAttribute("accountSettings", user.getAccountSettings());
        return  "social/settings";
    }

    /**
     * Handles update of a users details
     * @param updatedUser
     * @param principal
     * @param request
     * @return
     */
    @PostMapping("/account-settings/update-user")
    public String changeAccountDetails(@ModelAttribute("user") MyUser updatedUser, Principal principal, HttpServletRequest request){
        userService.changeAccountDetails(principal,updatedUser);
        //If user updates their password then force them to login again otherwise take them back
        if(!updatedUser.getPassword().isEmpty()){
            userService.setNewPasswordForUser(userService.getUserByEmail(principal.getName()), updatedUser.getPassword());
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            return "redirect:/login";
        }
        return "redirect:/social/account-settings";
    }

    /**
     * Handles the updating of a users privacy settings
     * @param updatedSettings
     * @param principal
     * @return
     */
    @PostMapping("/account-settings/update-privacy")
    public String updatePrivacySettings(@ModelAttribute("accountSetting")AccountSettings updatedSettings, Principal principal){
        userService.updatePrivacySettings(updatedSettings,principal);
        return "redirect:/social/account-settings";
    }

    /**
     * Handles the updating of a users notification settings
     * @param updatedSettings
     * @param principal
     * @return
     */
    @PostMapping("/account-settings/update-notifications")
    public String updateNotificationSettings(@ModelAttribute("accountSetting")AccountSettings updatedSettings, Principal principal){
        userService.updateNotificationSettings(updatedSettings, principal);
        return "redirect:/social/account-settings";
    }

    /**
     * Handles the payment a user has made
     * @param request
     * @return
     */
    @PostMapping("/account-settings/subscribe")
    public RedirectView subscribeUser(HttpServletRequest request){
        try{
            String cancelUrl = ServletUriComponentsBuilder.fromRequestUri(request).replacePath(null).build().toUriString()+"/social/account-settings/payment-cancel";
            String successUrl = ServletUriComponentsBuilder.fromRequestUri(request).replacePath(null).build().toUriString()+"/social/account-settings/payment-success";
            Payment payment = membershipService.createPayment(5.00,"GBP","paypal","SALE","Subscribed",cancelUrl,successUrl);
            for (Links link: payment.getLinks()) {
                if(link.getRel().equals("approval_url")){
                    return new RedirectView(link.getHref());
                }
            }
        }catch (PayPalRESTException e){
            System.err.println(e.getDetails());
        }
        return new RedirectView("/social/account-settings/payment-cancel");
    }

    /**
     * Handles payment success or failure if successful then it will simply subscribe the user
     * @param paymentId
     * @param payerId
     * @param principal
     * @param request
     * @return
     */
    @GetMapping("/account-settings/payment-success")
    public String paymentSuccess(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId, Principal principal, HttpServletRequest request){
        try{
            Payment payment = membershipService.executePayment(paymentId,payerId);
            if(payment.getState().equals("approved")){
                MyUser user = userService.getUserByEmail(principal.getName());
                membershipService.subscribeUser(user);
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                return "redirect:/social/account-settings";
            }
        }catch (PayPalRESTException e){
            System.err.println(e.getDetails());
        }
        return "redirect:/social/account-settings";
    }

    @GetMapping("/account-settings/payment-cancel")
    public String paymentCancel(){
        return "redirect:/social/account-settings";
    }
}
