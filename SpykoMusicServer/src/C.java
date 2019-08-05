public class C {
    public static void main(String [] args) throws Exception{
        B b = new B(1,2,3);
        A a = new A(1);
        A ab = new B(5,7);

        a.clone();

        System.out.println(a.compareTo(ab));
    }
}
