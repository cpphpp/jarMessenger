package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.function.Function;

import org.json.JSONObject;
import org.json.JSONArray;

public class Main extends Application {
    static Stage ourStage;

    ArrayList<String[]> connectionUsers;
    String[] connectionParameters;

    final String host = "http://www.anymessenger.heliohost.org/php/webHandler.php\n";
    final String ntfHost = "http://www.anymessenger.heliohost.org/php/ntfHandler.php";

    String username = "";
    String currentContact = "";

    int runningThread = 0;

    @Override
    public void start(Stage primaryStage) throws Exception {
        HttpURLConnection.setFollowRedirects(true); // defaults to true

        primaryStage.setTitle("jarMessenger");
        Parent root = FXMLLoader.load(getClass().getResource("logpage.fxml"));
        Scene scene = new Scene(root, 625, 440);
        scene.getStylesheets().add("sample/jarCss.css");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(625);
        primaryStage.setMinHeight(440);
        ourStage = primaryStage;
        System.out.println(getClass().getResource("texts/eula.txt"));
        logPage();
        //getDatabaseInfo();
    }
    
    void logIn(){
        Stage logStage = Functions.launchAlertWindow("Logging In...", "Wait please.", new Function<Void, Void>() {
            @Override
            public Void apply(Void aVoid) {
                return null;
            }
        });

        logStage.getScene().lookup("#okbutton").setVisible(false);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String params = "username=";
                    params += Functions.encodeURL(((TextField)ourStage.getScene().lookup("#username")).getText()) + "&";
                    params += "pass=";
                    params += Functions.encodeURL(((TextField)ourStage.getScene().lookup("#password")).getText()) + "&";
                    params += "login=1";
                    String response = Functions.HTMLRequest(host, params);
                    System.out.println(params);
                    System.out.println(response);

                    Label txt = (Label)logStage.getScene().lookup("#maintext");
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if(response.equals("\"TRUE\"")){
                                txt.setText("Entering chatroom..");
                                username = ((TextField)ourStage.getScene().lookup("#username")).getText();
                                chatPage();
                                logStage.close();
                            } else if(response.equals("\"FALSE\"")){
                                txt.setText("Wrong password or username.");
                                ((Button)logStage.getScene().lookup("#okbutton")).setVisible(true);
                            } else {
                                txt.setText("Server fault");
                                ((Button)logStage.getScene().lookup("#okbutton")).setVisible(true);
                                System.out.println("uh oh");
                                System.out.println(response);
                            }
                        }
                    });
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
    void register(boolean agreed){
        Scene scene = ourStage.getScene();

        try {
            if(!agreed){
                String text = Functions.fileToStringClassPath("texts/eula.txt");
                Functions.launchAlertWindow("By continuing, you agree with terms of use.", text, new Function<Void, Void>() {
                    @Override
                    public Void apply(Void aVoid) {
                        register(true);
                        return null;
                    }
                });
            } // alert bar
            else {
                System.out.println("Consent");

                String username = ((TextField)scene.lookup("#username")).getText();
                String password = ((TextField)scene.lookup("#password")).getText();

                Stage alertWindow = Functions.launchAlertWindow("Registering..", "Please wait.", new Function<Void, Void>() {
                    @Override
                    public Void apply(Void aVoid) {
                        return null;
                    }
                });
                Label mainText = (Label)alertWindow.getScene().lookup("#maintext");
                alertWindow.getScene().lookup("#okbutton").setVisible(false);

                if(!password.equals( ((TextField)scene.lookup("#repassword")).getText() )) {
                    mainText.setText("Passwords do not match.");
                    alertWindow.getScene().lookup("#okbutton").setVisible(true);
                    return;
                }
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String params = "username=" + Functions.encodeURL(username) + "&";
                        params += "pass=" + Functions.encodeURL(password) + "&";
                        params += "register=1";

                        try {
                            String response = Functions.HTMLRequest(host, params);
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    alertWindow.getScene().lookup("#okbutton").setVisible(true);

                                    if(response.equals("\"GOOD\"")){
                                        mainText.setText("You have been successfully registered.");
                                    } else if(response.equals("\"ERROR\"")){
                                        mainText.setText("User with this name already exists.");
                                    } else {
                                        mainText.setText("There was a problem registering you.");
                                        System.out.println(response);
                                    }
                                }
                            });

                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                });
                thread.start();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    void logPage (){
        try {
            Parent root = FXMLLoader.load(getClass().getResource("logpage.fxml"));
            ourStage.getScene().setRoot(root);
            ourStage.show();
        } catch (Exception e){
            e.printStackTrace();
        }
        Scene scene = ourStage.getScene();
        new Functions.NodeHandler(scene.getRoot(), ".logtext"){
            @Override
            void doForEveryNode(Node node){
                ((TextField) node).setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        System.out.println("Enter");
                        logIn();
                    }
                });
            }
            @Override
            void workList(){
                startDoForEveryNode();
            }
        };
        scene.lookup("#logbutton").setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                System.out.println("Log In");
                logIn();
            }
        });
        scene.lookup("#regbutton").setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                System.out.println("Sign Up");
                registerPage();
            }
        });

        Functions.cookie = null;
        currentContact = "";
    }
    void registerPage(){
        Scene scene = ourStage.getScene();
        try{
            ((AnchorPane)scene.lookup("#logpan")).getChildren().clear();
            URL inner = getClass().getResource("templates/templateRegister.fxml");
            FXMLLoader regLoader = new FXMLLoader(inner);
            regLoader.setRoot(scene.lookup("#logpan"));
            regLoader.load();
        } catch (Exception e){
            e.printStackTrace();
        }
        new Functions.NodeHandler(scene.getRoot(), ".logtext"){
            @Override
            void doForEveryNode(Node node){
                ((TextField)node).setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        System.out.println("Enter");
                        register(false);
                    }
                });
            }
            @Override
            void workList(){
                startDoForEveryNode();
            }
        };
        scene.lookup("#logbutton").setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                System.out.println("Reg In");
                register(false);
            }
        });
        scene.lookup("#backbutton").setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                logPage();
            }
        });
    }
    void chatPage(){
        try {
            Parent root = FXMLLoader.load(getClass().getResource("chatpage.fxml"));
            ourStage.getScene().setRoot(root);
            ourStage.show();
        } catch (Exception e){
            e.printStackTrace();
        }
        Scene scene = ourStage.getScene();
        scene.lookup("#quitbutton").setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                logPage();
            }
        });

        // Handle sending message.
        scene.lookup("#sendbutton").setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                sendMessage();
            }
        });
        scene.lookup("#messagetext").setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if(keyEvent.getCode() == KeyCode.ENTER)
                    sendMessage();
            }
        });

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ScrollPane scrollPane = (ScrollPane) scene.lookup("#scrollpane");
                scrollPane.setCache(false);
                scrollPane.lookup(".viewport").setCache(false);
                ((VBox)scrollPane.lookup("#messagebox")).prefWidthProperty().bind(scrollPane.widthProperty());
            }
        });

        ((Label)ourStage.getScene().lookup("#usernametext")).setText(username);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String response = Functions.HTMLRequest(host, "getChatList=1");
                    JSONArray responseJSON = new JSONArray(response);

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            VBox contactbar  = (VBox)scene.lookup("#contactbar");
                            contactbar.getChildren().clear();
                            for (int i = 0; i < responseJSON.length(); i++){
                                URL inner = getClass().getResource("templates/templateUser.fxml");
                                FXMLLoader regLoader = new FXMLLoader(inner);
                                regLoader.setRoot(contactbar);
                                try {
                                    regLoader.load();
                                }
                                catch (Exception e){
                                    e.printStackTrace();
                                }
                                // Handle userBox
                                AnchorPane userBox = (AnchorPane) contactbar.getChildren().get(i);
                                Label userBoxLabel = (Label)userBox.getChildren().get(1);
                                userBoxLabel.setText(responseJSON.get(i).toString());
                                userBox.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                    @Override
                                    public void handle(MouseEvent mouseEvent) {
                                        System.out.println(userBoxLabel.getText());
                                        getContact(userBoxLabel.getText());
                                    }
                                });
                            }

                            // Add contact button
                            URL inner = getClass().getResource("templates/templateAddContact.fxml");
                            FXMLLoader regLoader = new FXMLLoader(inner);
                            regLoader.setRoot(contactbar);
                            try {
                                regLoader.load();
                            }
                            catch (Exception e){
                                e.printStackTrace();
                            }
                            AnchorPane userBox = (AnchorPane) contactbar.getChildren().get(contactbar.getChildren().size() - 1);
                            userBox.setOnMouseClicked(new EventHandler<MouseEvent>() {
                                @Override
                                public void handle(MouseEvent mouseEvent) {
                                    addContactPage();

                                }
                            });
                        }
                    });

                    System.out.println(response);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        thread.start();

    }

    void getContact(String contact){
        boolean update = currentContact != contact;
        currentContact = contact;

        Scene scene = ourStage.getScene();
        AnchorPane topBar = (AnchorPane)scene.lookup("#topbar");
        ((Label)topBar.getChildren().get(0)).setText("Loading..");

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String params = "getChat=1&";
                    params += "chatter=" + Functions.encodeURL(contact);
                    String response = Functions.HTMLRequest(host, params);
                    System.out.println(params);
                    System.out.println(response);

                    JSONArray responseJSON = new JSONArray(response);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            scene.lookup("#inputarea").setVisible(true);

                            topBar.setVisible(true);
                            ((Label)topBar.getChildren().get(0)).setText(contact);
                            ((VBox)scene.lookup("#messagebox")).getChildren().clear();

                            VBox messageBox = (VBox) scene.lookup("#messagebox");
                            for (int i = 0; i < responseJSON.length(); i++) {
                                String sender = ((JSONArray)responseJSON.get(i)).get(0).toString();
                                String message = ((JSONArray)responseJSON.get(i)).get(1).toString();
                                insertMessage(messageBox, sender, message);
                            }
                            // Scroll to bottom
                            ((ScrollPane)scene.lookup("#scrollpane")).vvalueProperty().bind(messageBox.heightProperty());
                        }
                    });

                }
                catch (Exception e){
                    e.printStackTrace();
                }
                if(update)
                    initUpdate();

            }
        });
        thread.start();

    }
    void sendMessage(){
        Scene scene = ourStage.getScene();
        TextField messageField = (TextField) scene.lookup("#messagetext");
        String message = messageField.getText();
        System.out.println(message);
        insertMessage((VBox) scene.lookup("#messagebox"), username, messageField.getText());
        messageField.setText("");

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String params = "chatter=" + Functions.encodeURL(currentContact) + "&";
                    params += "message=" + Functions.encodeURL(message) + "&";
                    params += "sendMessage=1";
                    String response = Functions.HTMLRequest(host, params);

                    getContact(currentContact);

                }
                catch (Exception e){

                }
            }
        });
        thread.start();
    }
    void insertMessage(VBox messageBox, String username, String messageR){
        URL inner = getClass().getResource("templates/templateMessage.fxml");
        FXMLLoader regLoader = new FXMLLoader(inner);
        regLoader.setRoot(messageBox);
        try {
            regLoader.load();
        } catch (Exception e) {
            e.printStackTrace();
        }
        int childSize = messageBox.getChildren().size() - 1;
        AnchorPane messagePane = (AnchorPane) messageBox.getChildren().get(childSize);

        // Set color
        if (childSize % 2 == 0) {
            messagePane.setStyle("-fx-background-color:" + Colors.WHITE);
            Colors.lastColor = Colors.WHITE;
        }
        else {
            messagePane.setStyle("-fx-background-color:" + Colors.LIGHT_GRAY);
            Colors.lastColor = Colors.LIGHT_GRAY;
        }

        // Set text
        String sender = username;
        String message = messageR;

        HBox temp = (HBox) messagePane.getChildren().get(0);
        ((Label)temp.getChildren().get(0)).setText(sender);
        ((Label)temp.getChildren().get(1)).setText(message);
    }

    void addContactPage(){
        Stage alertWindow = Functions.launchAlertWindow("Add Contact", "", new Function<Void, Void>() {
            @Override
            public Void apply(Void aVoid) {
                return null;
            }
        });

        // Handle the window
        ScrollPane scrollPane = (ScrollPane)alertWindow.getScene().lookup("#scrollpane");
        URL inner = getClass().getResource("templates/templateAddContactDialog.fxml");
        FXMLLoader regLoader = new FXMLLoader(inner);
        regLoader.setRoot(scrollPane);
        try {
            regLoader.load();
        } catch (Exception e) {
            e.printStackTrace();
        }

        TextField contactField = (TextField) alertWindow.getScene().lookup("#contactname");

        Function<Void,Void> addFunction = new Function<Void, Void>() {
            @Override
            public Void apply(Void aVoid) {
                String contact = contactField.getText();
                Label mainText = ((Label)scrollPane.lookup("#maintext"));
                mainText.setText("Please wait...");

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String params = "contactName=" + Functions.encodeURL(contact) + "&";
                            params += "addContact=1";
                            String response = Functions.HTMLRequest(host, params);
                            System.out.println(params + " " + response);
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    if (response.equals("\"GOOD\"")) {
                                        getContact(contact);
                                        alertWindow.close();
                                    }
                                    else if (response.equals("\"ERROR\""))
                                        mainText.setText("No such user");
                                    else
                                        mainText.setText("There was a problem in the process.");
                                }
                            });

                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
                return null;
            }
        };

        alertWindow.getScene().lookup("#okbutton").setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                addFunction.apply(null);
            }
        });

        contactField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER){
                    addFunction.apply(null);
                }
            }
        });
    }

    void initUpdate(){
        String tempUser = new String(currentContact);
        int threadID = new Random().nextInt(256);
        runningThread = threadID;

        while(tempUser.equals(currentContact) && threadID  == runningThread){
            try {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }

                        String params = "username=" + Functions.encodeURL(username) + "&";
                        params += "chatter=" + Functions.encodeURL(currentContact)  + "&";
                        params += "checkNotification=1";
                        try {
                            String result = Functions.HTMLRequest(ntfHost, params);
                            System.out.println(result);
                            if (result.equals("\"NTF\"") && tempUser.equals(currentContact))
                                getContact(currentContact);
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
                thread.join();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}
