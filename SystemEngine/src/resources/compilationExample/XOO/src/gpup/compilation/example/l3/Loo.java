package gpup.compilation.example.l3;

import gpup.compilation.example.l2.Goo;
import gpup.compilation.example.l2.Koo;

public class Loo {
    private Goo goo;
    private Koo koo;

    public Loo(Goo goo, Koo koo) {
        this.goo = goo;
        this.koo = koo;
    }

    public Goo getGoo() {
        return goo;
    }

    public Koo getKoo() {
        return koo;
    }
}
