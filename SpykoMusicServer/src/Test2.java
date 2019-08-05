public class Test2 {
    public static int solution(String S) {
        int countOperations = 0;            //counts the number of needed operations (or "steps" as described in the assignment)
        StringBuilder newString = new StringBuilder(S);         //creating the StringBuilder from the given string in O(n) complexity
        int countZero = 0;                  //counts the amount of leading zero to the number
        int index = 0;                      //the index that is being used in the while loop
        /*The while loop iterates through the leading zeros of the number and counts them*/
        while (index < newString.length() && newString.charAt(index) != '1') {
            countZero++;
            index++;
        }
        /*This while loop iterates through the chars that come after the leading zeros of the number*/
        while(newString.length() != countZero){
            char currentChar = newString.charAt(newString.length()-1);          //the current char that is being manipulated. newString.length complexity - O(1)
            if(newString.length() == countZero+1 && currentChar == '0')      //if the current char is the last one to be manipulated and its value is '0' - then there is no need to continue
                return countOperations;
            switch (currentChar){
                case '1':                   //if the current char is 1, the number is odd
                    newString.setCharAt(newString.length()-1, '0');     //decrease 1 from the number by changing its less significant bit to 0
                    countOperations++;
                    break;
                case '0':                   //if the current char is 0, the number is even
                    newString.setLength(newString.length()-1);              //divide the number by 2 by removing the less significant bit from the number
                    countOperations++;
                    break;
            }
        }
        return countOperations;
    }

    public static void main(String [] args){
        String s = "111";
        System.out.println(solution(s));
    }
}
