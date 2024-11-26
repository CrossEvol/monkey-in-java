package org.monkey.parser;

public class ParseTracing {

    private final Parser parser;

    private static int traceLevel = 0;
    private static final String traceIdentPlaceHolder = "\t";

    public ParseTracing(Parser parser) {
        this.parser = parser;
    }

    public String trace(String msg) {
        incIdent();
        tracePrint("BEGIN " + msg);
        return msg;
    }

    public void untrace(String msg) {
        tracePrint("END " + msg);
        decIdent();
    }

    private void tracePrint(String fs) {
        System.out.printf("%s%s\n", identLevel(), fs);
    }

    private String identLevel() {
        return traceIdentPlaceHolder.repeat(traceLevel - 1);
    }

    private void incIdent() {
        traceLevel++;
    }

    private void decIdent() {
        traceLevel--;
    }
}
