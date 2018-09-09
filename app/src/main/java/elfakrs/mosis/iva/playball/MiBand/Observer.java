package elfakrs.mosis.iva.playball.MiBand;

public interface Observer {

    //mode: 1- miBand connected, 2- heartbeat, 3- steps
    void updateObserver (int mode, Object object);
}
