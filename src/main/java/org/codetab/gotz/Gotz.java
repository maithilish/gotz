package org.codetab.gotz;

public class Gotz {

    private static GotzEngine gotzEngine = new GotzEngine();

    private Gotz() {
    }

    public static void main(final String[] args) {
        gotzEngine.start();
    }

    static void setGotzEngine(GotzEngine gotzEngine){
        Gotz.gotzEngine = gotzEngine;
    }

    static GotzEngine getGotzEngine(){
        return Gotz.gotzEngine;
    }
}
