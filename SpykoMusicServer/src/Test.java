public class Test {
    public static int solution(int[] A) {
        int numTreesThatCanBeCut = 0;     //counts the amount of different possibilities to cut down one tree so the rest of the trees will be sorted in non-decreasing order
        int nextCheckedIndex;           //the index of the next integer to be checked in the second loop
        boolean notInNonDecreasingOrder = false;            //notInNonDecreasingOrder is true if the trees are not sorted in a non-decreasing order, false otherwise
        boolean currentTreeCantBeCut;           //true if the current tree in the first loop can't be cut, false otherwise
        /*Iterate through the array*/
        for(int i = 0; i < A.length; i++){
            if((i < A.length-1 && A[i] > A[i+1]) || (i > 0 && A[i] < A[i-1])){      //if the current tree is higher than the next one or lower than the one before - then this tree is a candidate to be cut
                notInNonDecreasingOrder = true;     //the trees are not in non-decreasing order
                currentTreeCantBeCut = false;
                /*Iterate through all the trees except the tree which is the candidate to be cut*/
                for(int j=0; j<A.length-1 && j!=i; j++){
                    if(j==i-1)          //if the current iterated tree is the one before the candidate tree, the next checked tree should be the one after the candidate
                        nextCheckedIndex = i+1;
                    else
                        nextCheckedIndex = j+1;
                    if(nextCheckedIndex < A.length && nextCheckedIndex != i+1 && A[j] > A[nextCheckedIndex]){       //if there is another tree, different from the candidate and the one before it, which is higher than the the one after it, that means there are no possible ways to cut one tree so the rest are sorted in non-decreasing order, and therefore the function returns 0
                        return 0;
                    }
                    if(nextCheckedIndex < A.length && A[j] > A[nextCheckedIndex]) {     //if the tree following the candidate tree is smaller than the tree before the candidate, that means you can't cut the candidate, or else the rest of the trees will not be sorted in a non-decreasing order
                        currentTreeCantBeCut = true;        //the candidate tree can't be cut
                        break;
                    }
                }
                if(!currentTreeCantBeCut)       //if you can cut the candidate
                    numTreesThatCanBeCut++;

            }
        }
        if(!notInNonDecreasingOrder)        //if all the trees are sorted in a non-decreasing order, that means they can all be cut
            numTreesThatCanBeCut = A.length;
        return numTreesThatCanBeCut;
    }


    public static void main(String [] args){
        int [] A = {0,1,0,3,4};
        System.out.println(solution(A));
    }
}
