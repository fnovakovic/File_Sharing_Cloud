package mutex;


import app.AppConfig;
import app.AppInfo;
import app.FileInfo;
import app.ServentInfo;
import servent.message.*;
import servent.message.mutex.TokenMessage;
import servent.message.util.MessageUtil;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;


public class SuzukiTokenMutex implements DistributedMutex {

    private volatile boolean haveToken = false;
    private static Boolean send = false;
    private AppInfo appInfo;
    private Map<String, FileInfo> files = new ConcurrentHashMap<>();
    private Queue<String> tokenWant = new ConcurrentLinkedQueue<>();

    public SuzukiTokenMutex(AppInfo appInfo) {
        this.appInfo = appInfo; //setujemo globalni objekat za svaki servent
    }

    @Override
    public Object lock(Boolean flag, String path) {  //metoda za lokovanje
       // wantLock = true;

        while (!haveToken) { //sve dok nemam token cekam

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        AppConfig.timestampedStandardPrint("Imam token i upisujem fajl");

        File f = new File(path); //ovo je vezano za fajl
        if (!f.exists()) {
            AppConfig.timestampedErrorPrint("File " + path + " doesn't exist.");
            return null;
        }

        if (f.isDirectory()) {
            AppConfig.timestampedErrorPrint(path + " is a directory and not a file.");
            return null;
        }

        if(flag == true){ //ovde ulazimo ako radimo dodavanje fajla

            try {
                BufferedReader reader = new BufferedReader(new FileReader(f));
                StringBuilder fileContent = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) { //citamo fajl
                    fileContent.append(line + "\n");
                }
                System.out.println("Ime fajla koji je upisan  " + f.getName());
                System.out.println("Kontent fajla koji je upisan " + fileContent);

                reader.close();

                FileInfo fi = new FileInfo(path,String.valueOf(fileContent),f.getName());
                //files.put(f.getName(),fi); //dodajemo putanju kontent i ime fajla u objekat fileinfo i njega dodajemo u listu fajlova
                //koju ce svaki servent imati

                if(appInfo.getFiles().isEmpty()){ //ako je globalna lista ne postoji onda znaci da je prazna i prvi put dodajemo u nju
                    FileInfo fi2 = new FileInfo(path,String.valueOf(fileContent),f.getName());//dodajemo putanju kontent i ime fajla u objekat fileinfo
                    Set<FileInfo> fils = new CopyOnWriteArraySet<>();
                    fils.add(fi2); //dodajemo fileInfo obj u set
                    appInfo.getFiles().put(appInfo.getId(),fils); //onda set dodajemo i listu globalnih fajlova sa kljucem id serventa
                    files.put(f.getName(),fi2); //dodajemo fajl kod nas u lokalnu listu

                }else{ //ako vec postoji globalna lista

                    if(appInfo.getFiles().containsKey(appInfo.getId())){ //gledamo da li postoji item u listi sa kljucem id serventa tj nas
                        FileInfo fi2 = new FileInfo(path,String.valueOf(fileContent),f.getName()); //pravimo obj fajla
                        for (Map.Entry<Integer, Set<FileInfo>> entry : appInfo.getFiles().entrySet()){
                            Set<FileInfo> fileInfoList = entry.getValue();
                            int key = entry.getKey();

                                if (key == (appInfo.getId())) { //prolazimo kroz listu dok ne nadjemo item sa kljucem kao sto smo mu prosledili tj nas
                                    fileInfoList.add(new FileInfo(path, String.valueOf(fileContent), f.getName()));//dodajemo fajl u listu globalnih fajlova sa nasim kljucem
                                }

                        }
                        files.put(f.getName(),fi2); //dodajemo fajl u nasu lokalnu listu

                    }else{//ako ne postoji item sa kljucem koji smo prosledili znaci da prvi put dodajemo listu pod tim kljucem

                        FileInfo fi2 = new FileInfo(path,String.valueOf(fileContent),f.getName()); //pravimo obj fajla
                        Set<FileInfo> fils = new CopyOnWriteArraySet<>();
                        fils.add(fi2); //dodajemo obj u set
                        appInfo.getFiles().put(appInfo.getId(),fils); //i onda dodajemo u globalni listu pod kljucem nasim tu listu
                        files.put(f.getName(),fi2);//dodajemo u nasu lokalnu listu
                    }

                }
                this.send = true;//setujemo flag na true zbog toga ako u unlocku on izadje napolje tj ne stigne zahtev za token, onda da mi kasnije kada stigne token mozemo da izvrsimo unlock ali da ne posaljemo ponovo update poruku svima
                unlock(); //radimo unlock


            } catch (IOException e) {
                AppConfig.timestampedErrorPrint("Couldn't read " + path + ".");
            }


        }else {

            System.out.println("Brisemo fajl sa imenom: " + f.getName());
            for (Map.Entry<String, FileInfo> fl: files.entrySet()){
                if(fl.getKey().equals(f.getName())){
                    files.remove(fl.getKey());//prolazimo kroz nasu lokalnu listu i brisemo fajl
                }
            }

            if(appInfo.getFiles().containsKey(appInfo.getId())) {
                for (Map.Entry<Integer, Set<FileInfo>> entry : appInfo.getFiles().entrySet()) { //prolazimo kroz globalnu listu
                    Set<FileInfo> fileInfoList = entry.getValue();
                    int key = entry.getKey();

                    if (key == (appInfo.getId())) { //uzimamo obj gde je kljuc sa nasim id-jem
                        for (FileInfo fil2 : fileInfoList) {
                            if (fil2.getName().equals(f.getName())) {
                                fileInfoList.remove(fil2);//prolazimo kroz fajlove i brisemo bas onaj koji smo trazili

                            }
                        }
                    }

                }
            }
            this.send = true;//setujemo flag na true zbog toga ako u unlocku on izadje napolje tj ne stigne zahtev za token, onda da mi kasnije kada stigne token mozemo da izvrsimo unlock ali da ne posaljemo ponovo update poruku svima
            unlock(); //radimo unlock

        }
        return null;
    }

    @Override
    public void unlock() {

        if(haveToken == true) {//ako imamo token onda radimo unlock
            long startTime = System.currentTimeMillis();
            if(send == true) { //da ne bismo poslali vise puda update

                Message updateMessage;
                updateMessage = new UpdateMessage(
                        AppConfig.myServentInfo,
                        null,
                        appInfo.getFiles(),//saljemo globalnu listu za update
                        "",
                        null,
                        1
                );

                System.out.println("Ovo su fajlovi kod mene");


                for (Map.Entry<String, FileInfo> entry : files.entrySet()) {
                    System.out.println("Fajl: " + entry.getKey());
                }


                for (Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
                    MessageUtil.sendMessage(updateMessage.changeReceiver(neighbor)); //saljemo update poruku svima
                }
                send = false; //obaramo na false
            }

            while (tokenWant.isEmpty() == true) { //cekamo dok ne stigne neki zahtev za token ako ne stigne u roku od 10 sec onda izlazimo tj stavljamo se na wait
                // System.out.println("Da li postoji neko ko ceka token " + !tokenWant.isEmpty());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (System.currentTimeMillis() - startTime >= 10000) {
                    System.out.println("Izlazi");
                    return; // Izlazak iz metode nakon 10 sekundi
                }
            }


            send = false;
            haveToken = false;


            if (tokenWant.isEmpty() == false) { //ako postoji neko u listi cekanja za token onda mu saljemo token
                System.out.println("Postoji, komsija koji ceka token je " + getHead());
                Integer servent = Integer.valueOf(getHead());
                tokenWant.remove();//brisemo tog iz nase liste tokena za cekanje
                for (String k : tokenWant)
                    System.out.println("Lista ljudi koji zahtevaju token koju saljem je " + k);

                Message removeMessage;
                removeMessage = new RemoveWantTokenMessage(
                        AppConfig.myServentInfo,
                        null,
                        null,
                        "",
                        servent
                );
                for (Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
                    MessageUtil.sendMessage(removeMessage.changeReceiver(neighbor));//saljemo poruku svima da obrisu taj servent iz svoje liste za cekanje tokena
                }

                MessageUtil.sendMessage(new TokenMessage(AppConfig.myServentInfo, AppConfig.getInfoById(servent), tokenWant, "", null, 1)); //saljemo token tom serventu

            }
        }
    }



    @Override
    public void update(Map<Integer,Set<FileInfo>> updateFiles, ServentInfo serventInfo) {


        appInfo.setFiles(updateFiles); //setujemo te fajlove koji su nam stigli u nasu globalnu listu

        System.out.println("OVO SU FAJLOVI KOD MENE ");


        for (Map.Entry<String, FileInfo> entry : files.entrySet()) {
            System.out.println("Fajl: " + entry.getKey());
        }

        System.out.println("Ovo je stanje sistema");
        for (Map.Entry<Integer, Set<FileInfo>> entry : appInfo.getFiles().entrySet()) {
            Set<FileInfo> fileInfoList = entry.getValue();
            int key = entry.getKey();
            System.out.println("Za servent " + key);


            Iterator<FileInfo> iterator = fileInfoList.iterator();


            while (iterator.hasNext()) {
                FileInfo fileInfo = iterator.next();


                System.out.println("Fajl: " + fileInfo.getName());


            }

        }

    }

    @Override
    public void get(String name) {

        int id = -1;
        long startTime = System.currentTimeMillis();
        if(files.containsKey(name)){ //provera da li je fajl u nasoj lokalnoj listi

            for(Map.Entry<String, FileInfo> fileInfo: files.entrySet()){

                if(fileInfo.getKey().equals(name)){ //ako ispisujemo ga
                    System.out.println("Fajl sa imenom " + fileInfo.getKey() + " je kod mene: " + fileInfo.getValue().getContent());
                }

            }


        }else{//ako nije kod nas onda gledamo kod koga je da bi smo mu poslali poruku
            while (id == -1) {


                for (Map.Entry<Integer,Set<FileInfo>> files2: appInfo.getFiles().entrySet()){
                    Set<FileInfo> fileInfoList = files2.getValue();
                    int key = files2.getKey();
                    Iterator<FileInfo> iterator = fileInfoList.iterator();


                    while (iterator.hasNext()) {
                        FileInfo fileInfo = iterator.next();

                        if(fileInfo.getName().equals(name)){
                            id = key; //pronalazimo onoga kod koga se nalazi taj fajl
                        }


                    }
                }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                if (System.currentTimeMillis() - startTime >= 10000) {
                    AppConfig.timestampedErrorPrint("Fajl pod tim imenom ne postoji, pokusajte ponovo");
                    return; // Izlazak iz metode nakon 10 sekundi
                }
            }

            Message askForFile;
            askForFile = new AskForFile(
                    AppConfig.myServentInfo,
                    AppConfig.getInfoById(id),
                    name,
                    null,
                    1
            );
            System.out.println("Trazim fajl sa imenom : " + name + " kod komsije sa id-jem: " + id);

            MessageUtil.sendMessage(askForFile.changeReceiver(id)); //saljemo zahtev tom serventu za taj fajl

        }

    }

    @Override
    public void findFile(String name,int id) { //zahtev za pronalazenje fajla kod nas
        System.out.println("Trazim fajl kod sebe sa imenom: " + name + " i treba da ga vratim serventu sa id-jem " + id );
        for(Map.Entry<String, FileInfo> fileInfo: files.entrySet()){ //prolazimo kroz nasu lokalnu listu

            if(fileInfo.getKey().equals(name)){ //trazimo fajl taj
                System.out.println("Nasao sam ga");
                FileInfo f = new FileInfo(fileInfo.getValue().getPath(),fileInfo.getValue().getContent(),fileInfo.getKey());
                Message returnFile; //pravimo obj i vracamo ga nazad tom serventu koji ga je zatrazio
                returnFile = new ReturnFile(
                        AppConfig.myServentInfo,
                        AppConfig.getInfoById(id),
                        f,
                        "",
                        1
                );

                MessageUtil.sendMessage(returnFile.changeReceiver(id)); //saljemo fajl tom serventu
            }

        }
    }

    @Override
    public void gotFile(FileInfo fileInfo, int id) { //stigao nam je zeljeni fajl
        System.out.println("Evo fajla koji smo trazili : " + fileInfo.getName() + " kontent: " + fileInfo.getContent() + " poslao ga je servent: " + id );
    }

    public void receiveToken(Queue<String> tokenWanttt) { //stigao nam je token
        AppConfig.timestampedStandardPrint("Stigao mi je token");
        //AppConfig.timestampedStandardPrint("Velicina liste za one koji cekaju servent koja mi je stigla je " + tokenWanttt.size());
        //AppConfig.timestampedStandardPrint("Velicina moje liste za cekanje tokena je " + tokenWant.size());
        for(String tokenWantt: tokenWanttt){
            AppConfig.timestampedStandardPrint("Evo liste onih koji zahtevaju token koja mi je stigla " + tokenWantt );
        }
        for(String tokenWantt: tokenWant){
            AppConfig.timestampedStandardPrint("U listi cekanja za token su pre " + tokenWantt);
        }

        for(String tokenWantt: tokenWanttt){
           tokenWant.add(tokenWantt); //dodajemo servente koji zahtevaju token sledeci u nasu listu
        }

            for(String tokenWantt: tokenWant){
                AppConfig.timestampedStandardPrint("U listi cekanja za token su posle " + tokenWantt);
            }

            haveToken = true; //setujemo flag za lock na true
    }

    public void sendTokenForward() { //saljemo token sledecem nasem serventu
        int nextNodeId = (AppConfig.myServentInfo.getId() + 1) % AppConfig.getServentCount();

        MessageUtil.sendMessage(new TokenMessage(AppConfig.myServentInfo, AppConfig.getInfoById(nextNodeId),tokenWant,"",null,1));
    }

    public boolean isHaveToken() {
        return haveToken;
    }

    public Queue<String> getTokenWant() {
        return tokenWant;
    }

    public void setTokenWant(Queue<String> tokenWant) {
        this.tokenWant = tokenWant;
    }
    public String getHead() {
        Iterator<String> iterator = tokenWant.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }
}
