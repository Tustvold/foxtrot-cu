package cam.ac.uk.foxtrot.sidefiller;

import java.util.ArrayList;

/**
 * Created by Milos on 25/02/2016.
 */
public class Edge
{
    private Point A, B; // Pointers to the edges vertices. The edge is directed as A->B
    private int aCount; // counts the number of times a occurs on the face
    private int bCount;
    private ArrayList<Integer> fromTriangle; // the triangle the edge comes from

    public Edge(Point A, Point B, int triangleidx)
    {
        this.A = A;
        this.B = B;
        aCount = 1;
        bCount = 1;
        fromTriangle = new ArrayList<>();
        fromTriangle.add(triangleidx);
    }

    /**
     * @param triangleidx the triangle index to add to the internal array
     */
    public void addTriangle(int triangleidx)
    {
        fromTriangle.add(triangleidx);
    }

    /**
     * @return true if the edge has a duplicate in the set
     */
    public boolean isDuplicate()
    {
        return aCount > 2 && bCount > 2;
    }

}
