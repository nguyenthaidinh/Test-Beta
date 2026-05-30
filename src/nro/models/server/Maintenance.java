package nro.models.server;

import nro.models.services.Service;
import nro.models.utils.Logger;

public class Maintenance {

    private static Maintenance instance;
    private int timeInSeconds;
    public static boolean isRunning = false;

    private Maintenance() {
    }

    public static Maintenance gI() {
        if (instance == null) {
            instance = new Maintenance();
        }
        return instance;
    }

    public void startCountdown() {
        if (!isRunning) {
            isRunning = true;
            this.timeInSeconds = 60;
            new Thread(this::runMaintenance, "Maintenance").start();
        }
    }

    public void startSeconds(int seconds) {
        if (!isRunning) {
            isRunning = true;
            this.timeInSeconds = seconds;
            new Thread(this::runMaintenance, "Maintenance").start();
        }
    }

    public void startImmediately() {
        if (!isRunning) {
            isRunning = true;
            Logger.log(Logger.YELLOW, "BAT DAU BAO TRI NGAY\n");
            ServerManager.gI().close();
        }
    }

    private void runMaintenance() {
        Logger.log(Logger.YELLOW, "Bat dau dem nguoc " + timeInSeconds + "s bao tri");

        while (timeInSeconds > 0) {
            try {
                sendRemainingTime();
                Thread.sleep(1000);
                timeInSeconds--;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Logger.log(Logger.YELLOW, "BAO TRI BAT DAU\n");
        ServerManager.gI().close();
    }

    private void sendRemainingTime() {
        String msg = "He thong se bao tri sau " + timeInSeconds + " giay nua. Hay thoat game de tranh mat du lieu.";
        Service.gI().sendThongBaoAllPlayer(msg);
        Logger.log(Logger.YELLOW, msg);
    }
}
