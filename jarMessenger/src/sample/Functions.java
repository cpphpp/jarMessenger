package sample;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.Charset;

public class Functions {
    static String cookie = "";

    static String encodeURL(String rawString){
        try {
            return URLEncoder.encode(rawString, StandardCharsets.UTF_8.toString());
        }
        catch (Exception e){
            e.printStackTrace();
            return "Error";
        }
    }

    static String HTMLRequest(String URL, String params) throws Exception{
        StringBuilder output = new StringBuilder("");

        //CookieManager cookieManager = new CookieManager();
        //String cookie = cookieManager.getCookie()
        URL request_url = new URL(URL);
        HttpURLConnection http_conn = (HttpURLConnection) request_url.openConnection();
        http_conn.setRequestMethod("POST");
        http_conn.setDoOutput(true);
        System.out.println("cookie: " + cookie);

        if(cookie != null)
            http_conn.setRequestProperty("Cookie", cookie);

        try (DataOutputStream wr = new DataOutputStream(http_conn.getOutputStream())) {
            wr.writeBytes(params);
            wr.flush();
        } catch (Exception e){}
        BufferedReader in = new BufferedReader(
                new InputStreamReader(http_conn.getInputStream()));
        String line;
        while((line = in.readLine()) != null)
            output.append(line);
        in.close();

        if(cookie == null)
            cookie = http_conn.getHeaderField("set-cookie");
        return output.toString();
    }
    static String fileToStringClassPath(String path){
        /*
        StringBuilder builderJoe = new StringBuilder();
        try {
            List<String> x = Files.readAllLines(
                    Paths.get(Main.class.getResource(path).toURI()), Charset.defaultCharset());
            for(int i = 0; i < x.size(); i++){
                builderJoe.append(x.get(i) + "\n");
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return builderJoe.toString();
         */
        byte[] data = {};
        try(InputStream in = Main.class.getResourceAsStream(path)){
            data = in.readAllBytes();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return new String(data, Charset.forName("UTF-8"));
    }
    static Stage launchAlertWindow(String header, String text, Function<Void,Void> onClickOK){
        Stage alertStage = new Stage();
        try {
            Parent alertRoot = FXMLLoader.load(Main.class.getResource("templates/templateDialog.fxml"));
            alertStage.setTitle("Alert");
            Scene alertScene = new Scene(alertRoot, 300, 200);
            alertStage.setScene(alertScene);
            alertStage.setResizable(false);
            alertStage.show();
            ((Label) alertRoot.lookup("#headertext")).setText(header);
            {
                Label mainText = (Label)alertRoot.lookup("#maintext");
                mainText.setText(text);
                // Make text bigger when touched
                mainText.setOnMouseClicked(new EventHandler<MouseEvent>(){
                    @Override
                    public void handle(MouseEvent mouseEvent){
                        mainText.setPrefHeight(500);
                        mainText.setPrefWidth(mainText.getPrefWidth() - 20); // Disable horizontal slidebar
                    }
                });
            }
            alertRoot.lookup("#okbutton").setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    alertStage.close();
                    onClickOK.apply(null);
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }
        return alertStage;
    }
    static class NodeHandler{
        Node node;
        String targetId;

        NodeHandler(Node node){
            this.node = node;
            workList();
        }
        NodeHandler(Node node, String id){
            this.node = node;
            this.targetId = id;
            workList();
        }

        void workList(){};

        void startDoForEveryNode(){
            Set<Node> temp = (Set<Node>) node.lookupAll(targetId);
            ArrayList<Node> a = new ArrayList<Node>();
            a.addAll(temp);
            for (int i = 0; i < a.size(); i++){
                try {
                    doForEveryNode(a.get(i));
                } catch (Exception e){
                    System.out.println(e);
                }
            }
        }
        void doForEveryNode(Node node){};
    }
}
