package cli.command;


import app.AppConfig;
import mutex.SuzukiTokenMutex;
import servent.message.Message;
import servent.message.mutex.TokenAskMessage;
import servent.message.util.MessageUtil;


import java.util.Iterator;
import java.util.Queue;


public class AddCommand implements CLICommand {
    private SuzukiTokenMutex mutex;
    private Queue<String> tokenWant;

    public AddCommand(SuzukiTokenMutex mutex, Queue<String> tokenWant) {
        this.mutex = mutex;
        this.tokenWant = tokenWant;
    }

    @Override
    public String commandName() {
        return "add";
    }

    @Override
    public void execute(String args) {

        if (args == null || args.isEmpty()) {
            AppConfig.timestampedStandardPrint("Invalid argument for add command. Should be add path.");
            return;
        }

        String path = "C:\\Users\\filip\\Desktop\\kids_proj_Filip_Novakovic_rn8820\\files/" + args;

        Message askMessage;
        if (mutex.isHaveToken() == true) { //ako imam token zakljucavam se i dodajem
            AppConfig.timestampedStandardPrint("Imam token i dodajem fajl");

            mutex.lock(true, path);

        } else { //ako nemam token saljem zahtev svima da hocu token i zakljucam se i cekam dok ne dobijem token
            if (AppConfig.IS_CLIQUE == true) { //ako je klika u pitanju


                AppConfig.timestampedStandardPrint("Nemam token zahtevam ga");
                tokenWant.add(String.valueOf(AppConfig.myServentInfo.getId())); // dodajem sebe i listu onih koji cekaju na token

                askMessage = new TokenAskMessage(
                        AppConfig.myServentInfo,
                        null,
                        tokenWant,
                        "",
                        null,
                        1
                );


                System.out.println("Lista cekanja kod mene je " + getHead());
                for (Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
                    MessageUtil.sendMessage(askMessage.changeReceiver(neighbor)); //saljem zahtev za token svima
                }

                mutex.lock(true, path); //zakljucavam se


            } else {

            }

        }
    }

    public String getHead() {
        Iterator<String> iterator = tokenWant.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return null; // or handle the case when the set is empty
    }
}
