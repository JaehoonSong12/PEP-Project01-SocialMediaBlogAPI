package com.ydjs.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ydjs.Model.Account;
import com.ydjs.Model.Message;
import com.ydjs.Service.AccountService;
import com.ydjs.Service.MessageService;
import io.javalin.Javalin;
import io.javalin.http.Context;

/**
 * Controller class that defines all the HTTP endpoints and handles incoming web requests.
 * 
 * TODOs: Wrote custom endpoints and handlers for the controller per test cases.
 * 
 * Architectural Pattern:
 * - Controller Layer (3-Layer Architecture) pattern handling HTTP routing, parsing JSON requests,
 *   and sending JSON responses via the Javalin framework.
 * - RESTful API Design Pattern: Provides stateless HTTP resource endpoints (GET, POST, DELETE, PATCH).
 */
public class SocialMediaController {
    
    // Design Pattern: Dependency Injection. Controller delegates business logic to services.
    private AccountService accountService;
    private MessageService messageService;

    /**
     * Default constructor for SocialMediaController.
     * Instantiates necessary service dependencies.
     */
    public SocialMediaController() {
        this.accountService = new AccountService();
        this.messageService = new MessageService();
    }

    /**
     * Configures and starts the Javalin API with defined routes.
     * 
     * Implementation document detail:
     * - Maps HTTP endpoints directly to their corresponding handler methods.
     * 
     * @return a Javalin app object which defines the behavior of the Javalin controller.
     */
    public Javalin startAPI() {
        Javalin app = Javalin.create();
        
        app.post("/register", this::registerHandler);
        app.post("/login", this::loginHandler);
        app.post("/messages", this::createMessageHandler);
        app.get("/messages", this::getAllMessagesHandler);
        app.get("/messages/{message_id}", this::getMessageByIdHandler);
        app.delete("/messages/{message_id}", this::deleteMessageByIdHandler);
        app.patch("/messages/{message_id}", this::updateMessageHandler);
        app.get("/accounts/{account_id}/messages", this::getMessagesByAccountIdHandler);

        return app;
    }

    /**
     * Handler for User Registration.
     * 
     * Implements specification:
     * - System Doc Section 2.1: User Registration.
     * 
     * @param context The Javalin Context object managing HTTP request and response.
     * @throws JsonProcessingException if mapping JSON payload to Account class fails.
     */
    private void registerHandler(Context context) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Account account = mapper.readValue(context.body(), Account.class);
        Account registeredAccount = accountService.registerAccount(account);
        // Implementation detail: Status 200 on success, 400 on Client error.
        if (registeredAccount != null) {
            context.json(registeredAccount);
            context.status(200);
        } else {
            context.status(400);
        }
    }

    /**
     * Handler for User Login.
     * 
     * Implements specification:
     * - System Doc Section 2.2: User Login.
     * 
     * @param context The Javalin Context object managing HTTP request and response.
     * @throws JsonProcessingException if mapping JSON payload to Account class fails.
     */
    private void loginHandler(Context context) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Account account = mapper.readValue(context.body(), Account.class);
        Account loggedInAccount = accountService.login(account);
        // Implementation detail: Status 200 on successful auth, 401 on Unauthorized.
        if (loggedInAccount != null) {
            context.json(loggedInAccount);
            context.status(200);
        } else {
            context.status(401);
        }
    }

    /**
     * Handler for creating a new Message.
     * 
     * Implements specification:
     * - System Doc Section 3.1: Create New Message.
     * 
     * @param context The Javalin Context object managing HTTP request and response.
     * @throws JsonProcessingException if mapping JSON payload to Message class fails.
     */
    private void createMessageHandler(Context context) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Message message = mapper.readValue(context.body(), Message.class);
        Message createdMessage = messageService.createMessage(message);
        // Implementation detail: Status 200 on success, 400 on Client error (bad data/user).
        if (createdMessage != null) {
            context.json(createdMessage);
            context.status(200);
        } else {
            context.status(400);
        }
    }

    /**
     * Handler to retrieve all messages.
     * 
     * Implements specification:
     * - System Doc Section 3.2: Retrieve All Messages.
     * 
     * @param context The Javalin Context object managing HTTP request and response.
     */
    private void getAllMessagesHandler(Context context) {
        // Implementation detail: Return JSON list. Returns empty [] via default List serialization if no items exist.
        context.json(messageService.getAllMessages());
        context.status(200);
    }

    /**
     * Handler to retrieve a single message by ID.
     * 
     * Implements specification:
     * - System Doc Section 3.3: Retrieve Message by ID.
     * 
     * @param context The Javalin Context object managing HTTP request and response.
     */
    private void getMessageByIdHandler(Context context) {
        int messageId = Integer.parseInt(context.pathParam("message_id"));
        Message message = messageService.getMessageById(messageId);
        // Implementation detail: Returns empty response body with status 200 if not found.
        if (message != null) {
            context.json(message);
        } else {
            context.status(200); // Response body empty
        }
    }

    /**
     * Handler to delete a single message by ID.
     * 
     * Implements specification:
     * - System Doc Section 3.4: Delete Message by ID.
     * 
     * @param context The Javalin Context object managing HTTP request and response.
     */
    private void deleteMessageByIdHandler(Context context) {
        int messageId = Integer.parseInt(context.pathParam("message_id"));
        Message deletedMessage = messageService.deleteMessageById(messageId);
        // Implementation detail: Idempotent operation. Returns deleted item if existed, otherwise empty body.
        if (deletedMessage != null) {
            context.json(deletedMessage);
        } else {
            context.status(200); // Response body empty
        }
    }

    /**
     * Handler to update the text of a single message by ID.
     * 
     * Implements specification:
     * - System Doc Section 3.5: Update Message by ID.
     * 
     * @param context The Javalin Context object managing HTTP request and response.
     * @throws JsonProcessingException if mapping JSON payload fails.
     */
    private void updateMessageHandler(Context context) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Message messageBody = mapper.readValue(context.body(), Message.class);
        int messageId = Integer.parseInt(context.pathParam("message_id"));
        
        Message updatedMessage = messageService.updateMessageText(messageId, messageBody.getMessage_text());
        // Implementation detail: Status 200 + full updated message on success, 400 on Client error.
        if (updatedMessage != null) {
            context.json(updatedMessage);
            context.status(200);
        } else {
            context.status(400);
        }
    }

    /**
     * Handler to retrieve all messages posted by a single user account.
     * 
     * Implements specification:
     * - System Doc Section 3.6: Retrieve Messages by Account ID.
     * 
     * @param context The Javalin Context object managing HTTP request and response.
     */
    private void getMessagesByAccountIdHandler(Context context) {
        int accountId = Integer.parseInt(context.pathParam("account_id"));
        // Implementation detail: Return JSON list, handles empty state safely.
        context.json(messageService.getAllMessagesByAccountId(accountId));
        context.status(200);
    }
}
