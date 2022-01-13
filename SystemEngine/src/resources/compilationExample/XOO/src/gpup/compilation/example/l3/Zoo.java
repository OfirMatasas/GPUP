package gpup.compilation.example.l3;

import gpup.compilation.example.l1.Boo;
import gpup.compilation.example.l2.Doo;

public class Zoo {
    private Doo doo;
    private Boo boo;

    public Zoo(Doo doo, Boo boo) {
        this.doo = doo;
        this.boo = boo;
    }

    public Doo getDoo() {
        return doo;
    }

    public Boo getBoo() {
        return boo;
    }
}
