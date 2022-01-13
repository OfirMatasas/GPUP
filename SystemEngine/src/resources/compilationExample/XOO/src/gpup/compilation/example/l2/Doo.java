package gpup.compilation.example.l2;

import gpup.compilation.example.l1.Boo;
import gpup.compilation.example.l1.Foo;

public class Doo {
    private Foo foo;
    private Boo boo;

    public Doo(Foo foo, Boo boo) {
        this.foo = foo;
        this.boo = boo;
    }

    public Foo getFoo() {
        return foo;
    }

    public Boo getBoo() {
        return boo;
    }
}
