public class A implements Cloneable, Comparable<A>{
    private static int st;

    public A() {
        System.out.println("Constructor A1");
        what();
    }

    public A(int i) throws RuntimeException{
        System.out.println("Constructor A2");
        what();
    }

    public A(int i, int j){
        System.out.println("Constructor A3");
        what();
    }

    @Override
    protected A clone() throws CloneNotSupportedException{
        return (A)super.clone();
    }

    @Override
    public int compareTo(A other){
        return 1;
    }

    public static void what(){

    }


    public void ga(){
        st = 5;
        System.out.println("ga in a");
    }

    public void da(){
        st = 5;
        System.out.println("ga in a");
    }
}
