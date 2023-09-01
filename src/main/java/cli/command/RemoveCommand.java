package cli.command;


import app.AppConfig;
import mutex.SuzukiTokenMutex;
import servent.message.Message;
import servent.message.mutex.TokenAskMessage;
import servent.message.util.MessageUtil;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RemoveCommand implements CLICommand {
    private SuzukiTokenMutex mutex;
    private Queue<String> tokenWant = new ConcurrentLinkedQueue<>();;

    public RemoveCommand(SuzukiTokenMutex mutex) {
        this.mutex = mutex;
    }
    @Override
    public String commandName() {
        return "remove";
    }

    @Override
    public void execute(String args) {
        if (args == null || args.isEmpty()) {
            AppConfig.timestampedStandardPrint("Invalid argument for add command. Should be add path.");
            return;
        }

        String path = "C:\\Users\\filip\\Desktop\\kids_proj_Filip_Novakovic_rn8820\\files/" + args;

        Message askMessage;
        if(mutex.isHaveToken() == true){ //ako imam token onda se zakljucavam i brisem fajl
            AppConfig.timestampedStandardPrint("Imam token i brisem fajl");

            mutex.lock(false,path);

        }else{ //ako nemam token onda saljem komsijama zahtev za token

            AppConfig.timestampedStandardPrint("Nemam token zahtevam ga");
            tokenWant.add(String.valueOf(AppConfig.myServentInfo.getId())); //dodajem sebe u listu onih koji zahtevaju token koju cu da posaljem komsijama
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
                MessageUtil.sendMessage(askMessage.changeReceiver(neighbor)); //saljem zahtev komsijama
            }

            mutex.lock(false,path);
        }
    }

    public String getHead() {
        Iterator<String> iterator = tokenWant.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }
}
