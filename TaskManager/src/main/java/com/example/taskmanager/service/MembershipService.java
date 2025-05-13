package com.example.taskmanager.service;

import com.example.taskmanager.domain.Task.Task;
import com.example.taskmanager.domain.User.MyUser;
import com.example.taskmanager.repo.Task.TaskRepository;
import com.example.taskmanager.repo.User.RoleRepository;
import com.example.taskmanager.repo.User.UserRepository;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class MembershipService {
    @Autowired
    private UserRepository userRepo;

    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private TaskRepository taskRepo;

    @Autowired
    private EmailService emailService;

    private final APIContext apiContext;

    public MembershipService(APIContext apiContext) {
        this.apiContext = apiContext;
    }

    /**
     * Creates the payment object for the transaction to take place
     * @param total
     * @param currency
     * @param method
     * @param intent
     * @param description
     * @param cancelUrl
     * @param successUrl
     * @return
     * @throws PayPalRESTException
     */
    public Payment createPayment(Double total,String currency,String method, String intent, String description,String cancelUrl, String successUrl) throws PayPalRESTException {
        //Get the amount and the currency (GBP for implementation reasons)
        Amount amount = new Amount(currency, String.format(Locale.forLanguageTag(currency),"%.2f",total));

        //Get transaction details
        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        //Get the method used
        Payer payer = new Payer();
        payer.setPaymentMethod(method);

        Payment payment = new Payment(intent,payer);
        payment.setTransactions(transactions);

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(successUrl);

        payment.setRedirectUrls(redirectUrls);

        return payment.create(apiContext);
    }

    /**
     * Executes the users payment
     * @param paymentId
     * @param payerId
     * @return
     * @throws PayPalRESTException
     */
    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        Payment payment = new Payment();
        payment.setId(paymentId);

        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);
        return payment.execute(apiContext,paymentExecution);
    }


    /**
     * Makes a user have a MEMBER role which would allow the user to user premium features
     * Also if a user is re-subscribing then set un-archived tasks to be archived again
     * @param user
     */
    public void subscribeUser(MyUser user){
        user.getRoles().add(roleRepo.findByName("MEMBER"));
        user.setSubscriptionStart(LocalDateTime.now());
        List<Task> tasks = user.getTasks();
        tasks.forEach((task) -> {
            if(task.getUnarchiveTimeStamp() != null && task.getUnarchiveTimeStamp().isAfter(LocalDate.now().minusDays(30))){
                task.setArchive(true);
                task.setUnarchiveTimeStamp(null);
                taskRepo.save(task);
            }
        });
        String emailContent = emailService.subscriptionPaymentConfirmed(user);
        emailService.sendMail(user.getEmail(),"Subscription Approved",emailContent);
        userRepo.save(user);
    }

}
