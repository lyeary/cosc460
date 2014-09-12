package simpledb;

import java.io.*;
import java.util.*;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

import sun.misc.IOUtils;

/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 *
 * @author Sam Madden
 * @see simpledb.HeapPage#HeapPage
 */
public class HeapFile implements DbFile {

	private File f;
	private TupleDesc td;
    /**
     * Constructs a heap file backed by the specified file.
     *
     * @param f the file that stores the on-disk backing store for this heap
     *          file.
     */
    public HeapFile(File f, TupleDesc td) {
        this.f = f;
        this.td = td;
    }

    /**
     * Returns the File backing this HeapFile on disk.
     *
     * @return the File backing this HeapFile on disk.
     */
    public File getFile() {
        return f;
    }

    /**
     * Returns an ID uniquely identifying this HeapFile. Implementation note:
     * you will need to generate this tableid somewhere ensure that each
     * HeapFile has a "unique id," and that you always return the same value for
     * a particular HeapFile. We suggest hashing the absolute file name of the
     * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
     *
     * @return an ID uniquely identifying this HeapFile.
     */
    public int getId() {
        return f.getAbsoluteFile().hashCode();
    }

    /**
     * Returns the TupleDesc of the table stored in this DbFile.
     *
     * @return TupleDesc of this DbFile.
     */
    public TupleDesc getTupleDesc() {
        return td;
    }

    // see DbFile.java for javadocs
    public Page readPage(PageId pid) {
    	int pgNo = pid.pageNumber(); 
        byte[] b = new byte[BufferPool.getPageSize()];
        ByteOutputStream ous = new ByteOutputStream();
        FileInputStream fis;
        int read = 0;
        Page pg;
        try {
			fis = new FileInputStream(f);
			fis.skip( (pgNo)*BufferPool.getPageSize() );
			while ( (read = fis.read(b)) != -1){ //reads up to b.length bytes of data from the input stream
				ous.write(b,0,read);
			}
	    ous.close();
	    fis.close();
	    pg = new HeapPage((HeapPageId)pid,b);
		} catch (Exception e) {
			throw new IllegalArgumentException();
		}
       return pg;
    }

    // see DbFile.java for javadocs
    public void writePage(Page page) throws IOException {
        // some code goes here
        // not necessary for lab1
    }

    /**
     * Returns the number of pages in this HeapFile.
     */
    public int numPages() {
        int fileSize = (int)f.length();
        int pageSize = BufferPool.PAGE_SIZE;
        return (int)Math.ceil(pageSize/fileSize);
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public DbFileIterator iterator(TransactionId tid) {
        Permissions perm = null;
        int numPages = this.numPages();
        int i;
        BufferPool bp = Database.getBufferPool();
        Page page;
        ArrayList<Tuple> tuples = new ArrayList<Tuple>();
        try {
        	for (i=0;i<numPages;i++) {
        		HeapPageId hpId = new HeapPageId(this.getId(), i);
        		page = bp.getPage(tid, hpId, perm);
        		Iterator<Tuple> itr = ((HeapPage)page).iterator();
        		while (itr.hasNext()) {
        			tuples.add(itr.next());
        		}
        	}
        	
    	}
        catch(Exception e) {
        	
        }
        Iterator<Tuple> iterator = tuples.iterator();
        // I think we need to implement DbFileIterator for a HeapFile...
        return null;
    }

}
