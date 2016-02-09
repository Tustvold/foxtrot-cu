package cam.ac.uk.foxtrot.voxelisation;

import javax.media.j3d.TriangleArray;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.io.IOException;

public abstract class MeshVoxeliser
{
    private Mesh mesh;

    private float cubeSize;    // size of standard cube (TODO to be set by user)
    private Point3i matrixDimensions; // dimesions of the block matrix
    private Point3f meshOffset;
    private TriangleArray initialTriangles;

    // determines the minimum values of the x,y, and z dimensions separately
    private Point3f getMaximumInitialCoodrinateBounds()
    {
        Point3f max = null;
        Point3f curr = new Point3f(0, 0, 0);
        int cnt = initialTriangles.getVertexCount();
        for (int i = 0; i < cnt; i++)
        {
            initialTriangles.getCoordinate(i, curr);
            if (max == null)
            {
                max = new Point3f(curr);
            } else
            {
                if (max.x < curr.x)
                    max.x = curr.x;
                if (max.y < curr.y)
                    max.y = curr.y;
                if (max.z < curr.z)
                    max.z = curr.z;
            }
        }
        return max;
    }

    // determines the aximum values of the x,y, and z dimensions separately
    private Point3f getMinimumInitialCoodrinateBounds()
    {
        Point3f min = null;
        Point3f curr = new Point3f(0, 0, 0);
        int cnt = initialTriangles.getVertexCount();
        for (int i = 0; i < cnt; i++)
        {
            initialTriangles.getCoordinate(i, curr);
            if (min == null)
            {
                min = new Point3f(curr);
            } else
            {
                if (min.x > curr.x)
                    min.x = curr.x;
                if (min.y > curr.y)
                    min.y = curr.y;
                if (min.z > curr.z)
                    min.z = curr.z;
            }
        }
        return min;
    }

    // determines the offset of one coordinate and sets the dimensions value
    private float calculateSingleOffset(int type, float minBound, float maxBound)
    {
        float diff = maxBound - minBound;
        int dimension = (int) Math.ceil(diff / cubeSize);
        switch (type)
        {
            case 0:
                matrixDimensions.x = dimension;
                break;
            case 1:
                matrixDimensions.y = dimension;
                break;
            case 2:
                matrixDimensions.z = dimension;
                break;
        }
        return (diff - (float) dimension * cubeSize / 2) + minBound;
    }

    // calculates the vector which need to be added to all the points, so that the mesh becomes centered
    // in the block matrix
    private void setMeshOffsetAndDetermineDimensions()
    {
        Point3f minBound = getMinimumInitialCoodrinateBounds();
        Point3f maxBound = getMaximumInitialCoodrinateBounds();
        float diffx = calculateSingleOffset(0, minBound.x, maxBound.x);
        float diffy = calculateSingleOffset(1, minBound.y, maxBound.y);
        float diffz = calculateSingleOffset(2, minBound.z, maxBound.z);

        meshOffset = new Point3f(diffx, diffy, diffz);
        mesh.setOffset(meshOffset);
    }

    private void shiftMeshByOffset()
    {
        Point3f min = null;
        Point3f curr = new Point3f(0, 0, 0);
        int cnt = mesh.getTriangles().getVertexCount();
        for (int i = 0; i < cnt; i++)
        {
            initialTriangles.getCoordinate(i, curr);
            curr.x += meshOffset.x;
            curr.y += meshOffset.y;
            curr.z += meshOffset.z;
            initialTriangles.setCoordinate(i, curr);
            mesh.getTriangles().setCoordinate(i, curr);
        }
    }

    /**
     * Generate a grid of blocks given a particular mesh
     * <p>
     * Elements in the grid with no mesh will be null
     *
     * @param mesh the mesh to voxelise
     * @return the grid of blocks
     */
    public Block[][][] voxeliseMesh(Mesh mesh, float cubeSize)
    {
        this.cubeSize = cubeSize;
        matrixDimensions = new Point3i(0, 0, 0);
        initialTriangles = mesh.getTriangles();

        setMeshOffsetAndDetermineDimensions();
        shiftMeshByOffset();
        Block[][][] blocks = generateBlocks(mesh);
        generateCustomParts(blocks);
        return blocks;
    }


    /**
     * Generate blocks for the given mesh
     * <p>
     * Elements in the grid with no mesh will be null
     *
     * @param mesh the mesh to generate blocks from
     * @return Grid of blocks in order x,y,z
     */
    private Block[][][] generateBlocks(Mesh mesh)
    {
        Block[][][] blocks = new Block[matrixDimensions.x][matrixDimensions.y][matrixDimensions.z];


        return blocks;
    }


    /**
     * Generate custom parts for Blocks which need it
     *
     * @param blocks Grid of blocks to generate custom parts for
     */
    abstract void generateCustomParts(Block[][][] blocks);

    /**
     * Write all the custom parts to the provided directory using the provided MeshIO
     *
     * @param directory the directory to write the files to
     * @param meshIO    the MeshIO object to use
     */
    void writeCustomPartsToDirectory(String directory, MeshIO meshIO) throws IOException
    {
        throw new RuntimeException("Not Implemented");
    }
}
