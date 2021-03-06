package simpledb;

/**
 * Unique identifier for HeapPage objects.
 */
public class HeapPageId implements PageId {

	private int tableId;
	private int pgNo;
	
    /**
     * Constructor. Create a page id structure for a specific page of a
     * specific table.
     *
     * @param tableId The table that is being referenced
     * @param pgNo    The page number in that table.
     */
    public HeapPageId(int tableId, int pgNo) {
        this.tableId = tableId;
        this.pgNo = pgNo;
    }

    /**
     * @return the table associated with this PageId
     */
    public int getTableId() {
        return this.tableId;
    }

    /**
     * @return the page number in the table getTableId() associated with
     * this PageId
     */
    public int pageNumber() {
        return this.pgNo;
    }

    /**
     * @return a hash code for this page, represented by the concatenation of
     * the table number and the page number (needed if a PageId is used as a
     * key in a hash table in the BufferPool, for example.)
     * @see BufferPool
     */
    public int hashCode() {
        //return Integer.parseInt(Integer.toString(tableId) + Integer.toString(pgNo));
    	int base = 0x0000FFFF;
    	int hash = (tableId & base) << 16;
    	hash = hash | (pgNo & base);
    	return hash;
    }

    /**
     * Compares one PageId to another.
     *
     * @param o The object to compare against (must be a PageId)
     * @return true if the objects are equal (e.g., page numbers and table
     * ids are the same)
     */
    public boolean equals(Object o) {
    	if (o == null) {
    		return false;
    	}
    	if (!(o instanceof HeapPageId)) {
    		return false;
    	}
        int other_tableId = ((HeapPageId) o).getTableId();
        int other_pgNo = ((HeapPageId) o).pageNumber();
        if (this.tableId == other_tableId && this.pgNo == other_pgNo) {
        	return true;
        }
        return false;
    }

    /**
     * Return a representation of this object as an array of
     * integers, for writing to disk.  Size of returned array must contain
     * number of integers that corresponds to number of args to one of the
     * constructors.
     */
    public int[] serialize() {
        int data[] = new int[2];

        data[0] = getTableId();
        data[1] = pageNumber();

        return data;
    }

}
