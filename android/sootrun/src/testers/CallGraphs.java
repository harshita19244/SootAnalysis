package testers;


import com.google.common.base.Splitter;

import java.util.stream.Stream;

public class CallGraphs
{
//    public static void main(String[] args) {
//        Stream<String> myList = Splitter.on(',').splitToStream("Hello, GFG, What's up ?");
//
//    }

}

class A
{
    public void foo() {
    }

    public void bar() {
        see();
        foo();
    }

    public void see(){
        check();
    }

    public void check(){
        rain();
    }

    public void rain(){
    }

}