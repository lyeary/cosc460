package simpledb;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BufferPool manages the reading and writing of pages into memory from
 * disk. Access methods call into it to retrieve pages, and it fetches
 * pages from the appropriate location.
 * <p/>
 * The BufferPool is also responsible for locking;  when a transaction fetches
 * a page, BufferPool checks that the transaction has the appropriate
 * locks to read/write the page.
 *
 * @Threadsafe, all fields are final
 */
public class BufferPool {
    /**
     * Bytes per page, including header.
     */
    public static final int PAGE_SIZE = 4096;

    private static int pageSize = PAGE_SIZE;

    /**
     * Default number of pages passed to the constructor. This is used by
     * other classes. BufferPool should use the numPages argument to the
     * constructor instead.
     */
    public static final int DEFAULT_PAGES = 50;
    private Hashtable<PageId, Page> bpool;
    private int numPages;
    private Hashtable<PageId, Long> pageAccessTime;
    private long timeAccessed;
   
    /**
     * Creates a BufferPool that caches up to numPages pages.
     *
     * @param numPages maximum number of pages in this buffer pool.
     */
    public BufferPool(int numPages) {
        //bp_pages = new Page[numPages]; -- didn't use numPages...
        bpool = new Hashtable<PageId, Page>();
        pageAccessTime = new Hashtable<PageId, Long>();
        this.numPages = numPages;
    }

    public static int getPageSize() {
        return pageSize;
    }

    // THIS FUNCTION SHOULD ONLY BE USED FOR TESTING!!
    public static void setPageSize(int pageSize) {
        BufferPool.pageSize = pageSize;
    }

    /**
     * Retrieve the specified page with the associated permissions.
     * Will acquire a lock and may block if that lock is held by another
     * transaction.
     * <p/>
     * The retrieved page should be looked up in the buffer pool.  If it
     * is present, it should be returned.  If it is not present, it should
     * be added to the buffer pool and returned.  If there is insufficient
     * space in the buffer pool, an page should be evicted and the new page
     * should be added in its place.
     *
     * @param tid  the ID of the transaction requesting the page
     * @param pid  the ID of the requested page
     * @param perm the requested permissions on the page
     */
    public Page getPage(TransactionId tid, PageId pid, Permissions perm)
            throws TransactionAbortedException, DbException {
        Page page = null;
        if(bpool.containsKey(pid)){
        	//System.out.println("page is in buffer pool!");
            page = bpool.get(pid);
        }
        else {
            int tableid = pid.getTableId();
            page = Database.getCatalog().getDatabaseFile(tableid).readPage(pid);
            if (bpool.size() >= numPages-1) {
            	this.evictPage();
            }
            bpool.put(pid, page);
            pageAccessTime.put(pid, System.currentTimeMillis());

        }
        return page;
    }
    


    /**
     * Releases the lock on a page.
     * Calling this is very risky, and may result in wrong behavior. Think hard
     * about who needs to call this and why, and why they can run the risk of
     * calling it.
     *
     * @param tid the ID of the transaction requesting the unlock
     * @param pid the ID of the page to unlock
     */
    public void releasePage(TransactionId tid, PageId pid) {
        // some code goes here
        // not necessary for lab1|lab2|lab3|lab4                                                         // cosc460
    }

    /**
     * Release all locks associated with a given transaction.
     *
     * @param tid the ID of the transaction requesting the unlock
     */
    public void transactionComplete(TransactionId tid) throws IOException {
        // some code goes here
        // not necessary for lab1|lab2|lab3|lab4                                                         // cosc460
    }

    /**
     * Return true if the specified transaction has a lock on the specified page
     */
    public boolean holdsLock(TransactionId tid, PageId p) {
        // some code goes here
        // not necessary for lab1|lab2|lab3|lab4                                                         // cosc460
        return false;
    }

    /**
     * Commit or abort a given transaction; release all locks associated to
     * the transaction.
     *
     * @param tid    the ID of the transaction requesting the unlock
     * @param commit a flag indicating whether we should commit or abort
     */
    public void transactionComplete(TransactionId tid, boolean commit)
            throws IOException {
        // some code goes here
        // not necessary for lab1|lab2|lab3|lab4                                                         // cosc460
    }

    /**
     * Add a tuple to the specified table on behalf of transaction tid.  Will
     * acquire a write lock on the page the tuple is added to and any other
     * pages that are updated (Lock acquisition is not needed until lab5).                                  // cosc460
     * May block if the lock(s) cannot be acquired.
     * <p/>
     * Marks any pages that were dirtied by the operation as dirty by calling
     * their markDirty bit, and updates cached versions of any pages that have
     * been dirtied so that future requests see up-to-date pages.
     *
     * @param tid     the transaction adding the tuple
     * @param tableId the table to add the tuple to
     * @param t       the tuple to add
     */
    public void insertTuple(TransactionId tid, int tableId, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
    	 try {
    		 ArrayList<Page> pages;
    		 DbFile dbFile = Database.getCatalog().getDatabaseFile(tableId);
    		 HeapFile hf = (HeapFile)dbFile;
    		 pages = hf.insertTuple(tid, t);
    		 for (Page page : pages) {
    			 page.markDirty(true,tid);
    			 bpool.put(page.getId(), page);
    		 }
    	} catch (DbException e){
    		 e.printStackTrace();
    	}
    }

    /**
     * Remove the specified tuple from the buffer pool.
     * Will acquire a write lock on the page the tuple is removed from and any
     * other pages that are updated. May block if the lock(s) cannot be acquired.
     * <p/>
     * Marks any pages that were dirtied by the operation as dirty by calling
     * their markDirty bit, and updates cached versions of any pages that have
     * been dirtied so that future requests see up-to-date pages.
     *
     * @param tid the transaction deleting the tuple.
     * @param t   the tuple to delete
     */
    public void deleteTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
    	int tableId = t.getRecordId().getPageId().getTableId();
    	DbFile dbFile = Database.getCatalog().getDatabaseFile(tableId);
    	HeapFile hf = (HeapFile)dbFile;
    	ArrayList<Page> pgs = hf.deleteTuple(tid, t);
  		for (Page page : pgs) {
			 page.markDirty(true,tid);
		 }
    }

    /**
     * Flush all dirty pages to disk.
     * NB: Be careful using this routine -- it writes dirty data to disk so will
     * break simpledb if running in NO STEAL mode.
     */
    public synchronized void flushAllPages() throws IOException {
    	for(PageId pid : bpool.keySet()){
			flushPage(pid);
    	}
    }

    /**
     * Remove the specific page id from the buffer pool.
     * Needed by the recovery manager to ensure that the
     * buffer pool doesn't keep a rolled back page in its
     * cache.
     */
    public synchronized void discardPage(PageId pid) {
        // some code goes here
        // only necessary for lab6                                                                            // cosc460
    }

    /**
     * Flushes a certain page to disk
     *
     * @param pid an ID indicating the page to flush
     */
    private synchronized void flushPage(PageId pid) throws IOException {
    	// only call this on dirty page to evict
    	if (pid == null) {
    		throw new NullPointerException();
    	}
//    	if (!bpool.containsKey(pid)) {
//    		throw new NoSuchElementException("Page is not in buffer pool.");
//    	}
    	System.out.println("This is a page: " + bpool.get(pid));
    	Page pagetoFlush = bpool.get(pid);
    	if (pagetoFlush.isDirty() != null) {
	    	Database.getCatalog().getDatabaseFile(pid.getTableId()).writePage(pagetoFlush);
	    	pagetoFlush.markDirty(false, pagetoFlush.isDirty());
    	}
   
    }
    
    /**
     * Write all pages of the specified transaction to disk.
     */
    public synchronized void flushPages(TransactionId tid) throws IOException {
        // some code goes here
        // not necessary for lab1|lab2|lab3|lab4                                                         // cosc460
    }

    /**
     * Discards a page from the buffer pool.
     * Flushes the page to disk to ensure dirty pages are updated on disk.
     */
    private synchronized void evictPage() throws DbException {
    	
		PageId evicted = null;
		long longestTime = 0;
		long curr_time = System.currentTimeMillis();
		Collection<PageId> allPages = pageAccessTime.keySet();
		for(PageId pg : allPages) {
			
			long temp = (curr_time - pageAccessTime.get(pg));
			if(temp > longestTime) {			// if we find an older page
			    evicted = pg;					// set pid to be evicted
			    longestTime = temp;
			}
		}
		if (evicted == null) {
			throw new DbException("All pages are dirty.");
		}
		else try {
			System.out.println("evicted null" + evicted);
			System.out.println("NULL??" + bpool.get(evicted));
        	System.out.println("calling flushpage");
        	if (bpool.containsKey(evicted)) {
	            flushPage(evicted);
	            bpool.remove(evicted);
	            pageAccessTime.remove(evicted);
        	}
    } 
        catch (IOException e) {
            e.printStackTrace();
        }   
           
    }

}
