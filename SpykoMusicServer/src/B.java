public class B extends A{

    public B() {
        System.out.println("Constructor B1");
    }

    public B(int i, int j, int k) throws Exception{
        this(i,j);
        System.out.println("Constructor B2");
    }

    public B(int i, int j) throws Exception{
        super(i);
        System.out.println("Constructor B3");
    }
    
    public void ga(){

        System.out.println("ga in b");
    }
}
