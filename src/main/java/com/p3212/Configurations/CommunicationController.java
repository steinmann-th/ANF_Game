package com.p3212.Configurations;

import com.p3212.EntityClasses.FriendsRequest;
import com.p3212.main.BotListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.p3212.EntityClasses.PrivateMessage;
import com.p3212.EntityClasses.StompPrincipal;
import com.p3212.EntityClasses.User;
import com.p3212.Services.FriendsRequestService;
import com.p3212.Services.MessagesService;
import com.p3212.Services.UserService;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class CommunicationController {

    @Autowired
    private MessagesService messageServ;
    @Autowired
    private UserService userServ;
    @Autowired
    private FriendsRequestService requestServ;
    @Autowired
    private WebSocketsController notifServ;

    @Autowired
    BotListener botListener;
    
    @Autowired
    WebSocketsController wsController;

    /**
     * Sends a message. Receives two Strings (receiver login and Message itself), takes sender object from SecurityContext
     */
    @PostMapping("/profile/messages")
    public ResponseEntity<String> sendMessage(@RequestParam("message") String message, @RequestParam("receiver") String receiver) {
        try {
            User recvr = userServ.getUser(receiver);
            if (recvr == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"User " + receiver + " wasn't found.\"}");
            User sender = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            PrivateMessage msg = new PrivateMessage(recvr, sender);
            msg.setIsRead(false);
            msg.setMessage(message);
            messageServ.addMessage(msg);
            String wsmessage = SecurityContextHolder.getContext().getAuthentication().getName() + ":" + message;
            wsController.send(new StompPrincipal(receiver), wsmessage);
            return ResponseEntity.status(HttpStatus.OK).body(msg.getSendingDate().toString());
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.getMessage());
        }
    }

    @GetMapping("/profile/messages/unread")
    public ResponseEntity<?> getUnreadMessages() {
        try {
            User user = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            return ResponseEntity.status(HttpStatus.OK).body(messageServ.getUnreadMessages(user));
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getMessage());
        }
    }

    @GetMapping("/profile/dialogs")
    public ResponseEntity<?> getDialogs() {
        User user = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
        List<String> list = messageServ.getDialogs(user);
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    /**
     * Returns all messages between User from SecurityContext and User with username provided
     */
    @GetMapping("/profile/messages/dialog")
    public ResponseEntity<?> getMessagesFromDialog(@RequestParam String secondName) {
        try {
            User sen = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            User rec = userServ.getUser(secondName);
            if (rec == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"User with name " + secondName + " wasn't found.\"}");
            return ResponseEntity.status(HttpStatus.OK).body(messageServ.getAllFromDialog(sen, rec));
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getMessage());
        }
    }

    /**
     * Deletes a message. User can only delete his own messages.
     */
    @DeleteMapping("/profile/messages/{id}")
    public ResponseEntity<String> deleteMessage(@PathVariable int id) {
        try {
            User applier = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            PrivateMessage msg = messageServ.getMessage(id);
            if (msg == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"Message with id = " + id + " wasn't found.\"}");
            if (msg.getSender().equals(applier)) {
                messageServ.removeMessage(id);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"User can only remove his own messages.\"}");
            }
            return ResponseEntity.status(HttpStatus.OK).body("{\"Message is deleted.\"}");
        } catch (Exception error) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.getMessage());
        }
    }

    /**
     * Sets message as read. Takes receiver from SecurityContext.
     */
    @PostMapping("/profile/messages/{id}/read")
    public ResponseEntity<String> setMessageRead(@PathVariable int id) {
        try {
            User recvr = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            PrivateMessage message = messageServ.getMessage(id);
            if (message == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"Message with id " + id + " wasn't found.\"}");
            if (message.getReceiver().equals(recvr)) {
                messageServ.setRead(id);
                return ResponseEntity.status(HttpStatus.OK).body("{\"Message 'is read' state is confirmed.\"}");
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"User can only set read status on messages (s)he received.\"}");
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error.getMessage());
        }
    }

    /**
     * Takes a user from SecurityContext and a User from username and removes either request from *username*
     * to current User or from current User to *username*. See @Query in FriendsRequestRepository
     */
    @DeleteMapping("/profile/friends/requests")
    public ResponseEntity<String> deleteRequest(@RequestParam String username, @RequestParam String type) {
        try {
            String wsMessage = SecurityContextHolder.getContext().getAuthentication().getName();
            User sendr;
            User recvr;
            if (type.equalsIgnoreCase("in")) {
                sendr = userServ.getUser(username);
                recvr = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
                wsMessage = "decline:" + wsMessage;
                wsController.sendSocial(new StompPrincipal(recvr.getLogin()), "request:-"+username);
            } else {
                sendr = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
                recvr = userServ.getUser(username);
                wsMessage = "request:-" + wsMessage;
                wsController.sendSocial(new StompPrincipal(sendr.getLogin()), "request:/"+username);
            }
            if (!(requestServ.requestedUsers(sendr).contains(recvr)))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"Request wasn't found.\"}");
            if (recvr == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"User with name " + username + " wasn't found.\"}");
            requestServ.removeRequest(recvr, sendr);
            wsController.sendSocial(new StompPrincipal(username), wsMessage);
            return ResponseEntity.status(HttpStatus.OK).body("{\"Request is deleted.\"}");
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.getMessage());
        }
    }

    @GetMapping("/friends/requests/outgoing")
    public ResponseEntity<?> getOutgoingRequests() {
        try {
            User sender = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            return ResponseEntity.status(HttpStatus.OK).body(requestServ.requestedUsers(sender));
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.getMessage());
        }
    }

    @GetMapping("/friends/requests/incoming")
    public ResponseEntity<?> getIncomingRequests() {
        try {
            User receiver = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            return ResponseEntity.status(HttpStatus.OK).body(requestServ.requestingUsers(receiver));
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.getMessage());
        }
    }

    @PostMapping("/profile/friends/requests")
    public ResponseEntity<String> sendRequest(@RequestParam String username) {
        try {
            String wsMessage = "request:+" + SecurityContextHolder.getContext().getAuthentication().getName();
            User receiver = userServ.getUser(username);
            if (receiver == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"User with name " + username + " wasn't found.\"}");
            User sender = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            FriendsRequest request = new FriendsRequest(sender, receiver);
            requestServ.addRequest(request);
            wsController.sendSocial(new StompPrincipal(username), wsMessage);
            wsController.sendSocial(new StompPrincipal(sender.getLogin()), "request:o"+username);
            return ResponseEntity.status(HttpStatus.CREATED).body("{\"Friend request created!\"}");
        } catch (Throwable th) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(th.getMessage());
        }
    }

    @PostMapping("/profile/friends")
    public ResponseEntity<String> addFriend(@RequestParam String login) {
        try {
            String wsMessage = "friend:+" + SecurityContextHolder.getContext().getAuthentication().getName();
            User acceptor = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            User sender = userServ.getUser(login);
            int reqId = -1;
            for (FriendsRequest req: acceptor.getFriendRequestsIn()) {
                if (req.getRequestingUser().getLogin().equals(login)) {
                    reqId = req.request_id;
                    break;
                }
            }
            if (reqId!=-1)
                userServ.addFriend(acceptor, sender);
            else
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"No friend request was found.\"}");
            requestServ.removeById(reqId);
            wsController.sendSocial(new StompPrincipal(login), wsMessage);
            wsController.sendSocial(new StompPrincipal(acceptor.getLogin()), "friend:o"+login);
            return ResponseEntity.status(HttpStatus.CREATED).body("{\"Friends relationship is created.\"}");
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error.getMessage());
        }
    }

    @DeleteMapping("/profile/friends")
    public ResponseEntity<String> removeFriend(@RequestParam String username) {
        try {
            String wsMessage = "friend:-" + SecurityContextHolder.getContext().getAuthentication().getName();
            User remover = userServ.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
            User removed = userServ.getUser(username);
            if (removed == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"User with name " + username + " wasn't found.\"}");
            if (!(remover.getFriends().contains(removed)))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"User " + username + " is not a friend of a user " + remover.getLogin() + ".\"}");
            userServ.removeFriend(remover, removed);
            wsController.sendSocial(new StompPrincipal(username), wsMessage);
            wsController.sendSocial(new StompPrincipal(remover.getLogin()), "friend:/"+username);
            return ResponseEntity.status(HttpStatus.OK).body("{\"Friends relationship is removed.\"}");
        } catch (Throwable error) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.getMessage());
        }
    }

    @PostMapping("/admin/chat")
    public ResponseEntity<String> sendAdminWarning(@RequestBody String warning) {
        /*Message notif = new Message();
        notif.setAuthor("SYSTEM");
        notif.setText(warning);
        notifServ.notify(notif);*/
        notifServ.notify("SYSTEM:"+warning);
        return ResponseEntity.status(HttpStatus.CREATED).body("{\"Warning is sent.\"}");
    }

//    @GetMapping("/sendinvite")
//    public ResponseEntity<String> inviteToFight(@RequestParam String type, @RequestParam String username) {
//        String author = SecurityContextHolder.getContext().getAuthentication().getName();
//        wsController.sendInvitation(username, author, type);
//        return ResponseEntity.ok().body("OK");
//    }
    
}
