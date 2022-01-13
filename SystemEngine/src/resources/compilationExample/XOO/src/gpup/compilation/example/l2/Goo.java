package gpup.compilation.example.l2;

import gpup.compilation.example.l1.Boo;
import gpup.compilation.example.l1.Moo;

public class Goo {
    private Boo boo;
    private Moo moo;

    public Goo(Boo boo, Moo moo) {
        this.boo = boo;
        this.moo = moo;
    }

    public Boo getBoo() {
        return boo;
    }

    public Moo getMoo() {
        return moo;
    }
}
