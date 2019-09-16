
/**
 *
 * WAVLTree
 *
 * An implementation of a WAVL Tree with
 * distinct integer keys and info
 *
 */

public class WAVLTree {
	private IWAVLNode root;		//pointer for the root of the tree
	private IWAVLNode external; //Represents external leaves
	private IWAVLNode min;		//pointer for the node with minimum key
	private IWAVLNode max;		//pointer for the node with maximum key

	/**constructor for empty tree*/
	public WAVLTree () {
		root = null;
		min = null;
		max = null;
		external = new WAVLNode();
	}
	
	/**
	 * public boolean empty()
	 *
	 * returns true if and only if the tree is empty
	 *
	 *complexity: O(1)
	 */
	public boolean empty() {
		return root == null;
	}

	/**
	 * private IWAVLNode treePosition(int k)
	 * 
	 * returns the last item was encountered 
	 * during the search for the node with key k.
	 * 
	 * complexity: O(logn) */
	private IWAVLNode treePosition(int k){
		IWAVLNode cur = root;
		IWAVLNode prev = cur;
		while (cur!=external && cur!=null){
			prev = cur;
			if (k==cur.getKey())
				return cur;
			else if (k<cur.getKey())
				cur = cur.getLeft();
			else
				cur = cur.getRight();
		}
		return prev; 
	}
	
	/**
	 * public String search(int k)
	 *
	 * returns the info of an item with key k if it exists in the tree.
	 * otherwise, returns null
	 * 
	 * complexity: O(logn)
	 */
	public String search(int k) {
		IWAVLNode node = treePosition(k);
		if (node==null || node.getKey()!=k) {
			return null;
		}
		return node.getValue();
		
		
	}

	/**
	 * public int insert(int k, String i)
	 *
	 * inserts an item with key k and info i to the WAVL tree.
	 * the tree must remain valid (keep its invariants).
	 * returns the number of rebalancing operations, or 0 if no rebalancing operations were necessary.
	 * returns -1 if an item with key k already exists in the tree.
	 * 
	 * complexity: O(logn)
	 */
	public int insert(int k, String i) {
		IWAVLNode parent = treePosition(k);
		IWAVLNode newNode = new WAVLNode(k,i);
		//insert into empty tree
		if (parent == null)
		{
			root = newNode;
			min = newNode;
			max = newNode;
			return 0;
		}
		//an item with key k already exists in the tree
		if (parent.getKey() == k) {
			return -1;
		}
		//insert as right child
		if (parent.getKey()<k) {
			parent.setRight(newNode);
			
			if (k>max.getKey()){//update max
				max = newNode;
			}
		}
		//insert as left child
		else {
			parent.setLeft(newNode);
			
			if (k<min.getKey()){//update min
				min = newNode;
			}
		}

		parent.incSize();
		return insertRebalance(newNode);

	}
		
	/**
	 * public int delete(int k)
	 *
	 * deletes an item with key k from the binary tree, if it is there;
	 * the tree must remain valid (keep its invariants).
	 * returns the number of rebalancing operations, or 0 if no rebalancing operations were needed.
	 * returns -1 if an item with key k was not found in the tree.
	 * 
	 * complexity: O(logn)
	 */
	public int delete(int k)
	{
		IWAVLNode nodeToDel = treePosition(k);
		
		//an item with key k was not found in the tree
		if (nodeToDel==null||nodeToDel.getKey()!=k) {
			return -1;
		}
		
		IWAVLNode child;
		
		//delete a node with two children
		if (nodeToDel.getLeft()!=external && nodeToDel.getRight()!=external) {
			IWAVLNode successor = nodeToDel.successor();
			child = successor.getRight(); //the successor doesn't have a left child
			child.replaceParentWith(successor); //delete the successor
			successor.replaceNodes(nodeToDel); //replace nodeToDel with his successor
		}
		
		//delete an unary node or an internal leaf
		else {
			if (nodeToDel.getLeft()!=external){
				child = nodeToDel.getLeft();
				child.replaceParentWith(nodeToDel);
			}
			else {
				child = nodeToDel.getRight();
				if (child==external && root==nodeToDel){//the nodeToDel is the root and is a leaf
					root = null;
					min =null;
					max = null;
					return 0;
				}
				child.replaceParentWith(nodeToDel);
			}
		}
		//update min
		if (nodeToDel == min){
			if (nodeToDel.getRight().isRealNode()){
				min = nodeToDel.getRight();
			}
			else {
				min = nodeToDel.getParent();
			}
		}
		//update max
		if (nodeToDel == max){
			if (nodeToDel.getLeft().isRealNode()){
				max = nodeToDel.getLeft();
			}
			else {
				max = nodeToDel.getParent();
			}
		}
		
		if (child!=root){
			IWAVLNode p = child.getParent();
			p.decSize();
			if (p.isLeaf()){ //p is a 2,2 leaf
				p.demote();
				return 1+deleteRebalance(p);
			}
		}
		
		return deleteRebalance(child);
	}
	
	/**
	 * private int insertRebalance(IWAVLNode x)
	 * 
	 * performs a sequence of rebalancing operations after insertion starting at node x and following a path upwards in the tree .
	 * returns the number of rebalancing operations occurred.
	 * 
	 * complexity: O(logn) */
	private int insertRebalance(IWAVLNode x){
		int ctr = 0;
		
		//x is the root and wer'e done
		if (x==root) {
			return ctr;
		}
		
		IWAVLNode z = x.getParent();
		IWAVLNode y,b;
		
		int k = z.getRank();
		if (k==x.getRank()) {
			
			if (x==z.getLeft()){ //is x a left child
				y = z.getRight();
				b = x.getRight();
			}
			else {
				y = z.getLeft();
				b = x.getLeft();
			}
			
			if (k==y.getRank()+1) // rank diff is 1 between x's brother and its parent
			{
				z.promote();;
				ctr=1+insertRebalance(z);
			}
			else { // rank diff is 2 between x's brother and parent
				if (k==b.getRank()+2) //single rotation
				{
					singleRotate(x);
					//fix ranks after rotation
					z.demote();
					ctr+=1;
				}
				else{ // double rotation
					doubleRotate(b);
					//fix ranks after rotations
					x.demote();
					z.demote();
					b.promote();
					ctr+=2;
				}
			}
		}
		return ctr;
	}
	
	/**
	 * private int deleteRebalance(IWAVLNode x)
	 * 
	 * performs a sequence of rebalancing operations after deletion, starting at node x and following a path upwards in the tree .
	 * returns the number of rebalancing operations occurred.
	 * 
	 * complexity: O(logn) */
	private int deleteRebalance(IWAVLNode x) {
		int ctr = 0;
		
		//x is the root and wer'e done
		if (x==root) {
			return ctr;
		}
		
		IWAVLNode z = x.getParent();
		IWAVLNode y,a,b;
		
		int k = z.getRank();
		if (k-x.getRank()==3) {
			
			if (x==z.getLeft()){ //is x a left child
				y = z.getRight();
				a = y.getLeft();
				b = y.getRight();
			}
			else {
				y = z.getLeft();
				a = y.getRight();
				b = y.getLeft();
			}
			
			if (k-y.getRank()==2) // rank diff is 2 between x's brother and its parent
			{
				z.demote();
				ctr=1+deleteRebalance(z);
			}
			else { // rank diff is 1 between x's brother and parent
				if (y.getRank()-a.getRank()==2 && y.getRank()-b.getRank()==2){ // double demote
					z.demote();
					y.demote();
					ctr=2+deleteRebalance(z);
				}
				else if ((y.getRank()-b.getRank()==1)){ // single rotation
					singleRotate(y);
					//fix ranks after rotations
					y.promote();
					z.demote();
					
					if (z.isLeaf()){ // z is a 2,2 leaf
						z.demote();
					}
					
					ctr+=1;
				}
				else { // double rotate
					doubleRotate(a);
					//fix ranks after rotations
					y.demote();
					z.demote();
					z.demote();
					a.promote();
					a.promote();
					ctr+=2;
				}
			}
		}
		return ctr;
		
	}

	/**
	 * private void singleRotate(IWAVLNode x)
	 * 
	 * if x is a left child, makes a right rotation at the subtree of x.parent , 
	 * otherwise, makes a left rotation at the subtree of x.parent
	 * 
	 * precondition: x is not the root of the tree
	 * 
	 * complexity: O(1)*/
	private void singleRotate(IWAVLNode x){
		IWAVLNode y = x.getParent();
		
		x.replaceParentWith(y);
		
		if (x==y.getLeft()){ //right rotate
			y.setLeft(x.getRight());
			x.setRight(y);
		}
		else { //left rotate
			y.setRight(x.getLeft());
			x.setLeft(y);
		}
	
		y.updateSize();
		x.updateSize();
	}
	
	/**
	 * private void doubleRotate(IWAVLNode z)
	 * 
	 * makes a rotation between z and his parent,
	 * and another rotation between z and his new parent.
	 * 
	 * precondition: the depth of z >= 2
	 * 
	 * complexity: O(1)*/
	private void doubleRotate(IWAVLNode z){
		singleRotate(z);
		singleRotate(z);
	}	
	
	/**
	 * public String min()
	 *
	 * Returns the info of the item with the smallest key in the tree,
	 * or null if the tree is empty
	 * 
	 * complexity: O(1)
	 */
	public String min()
	{
		if (empty()){
			return null;
		}
		return min.getValue();
	}

	/**
	 * public String max()
	 *
	 * Returns the info of the item with the largest key in the tree,
	 * or null if the tree is empty
	 * 
	 * complexity: O(1)
	 */
	public String max()
	{
		if (empty()){
			return null;
		}
		return max.getValue();
	}

	/**
	 * public int[] keysToArray()
	 *
	 * Returns a sorted array which contains all keys in the tree,
	 * or an empty array if the tree is empty.
	 * 
	 * complexity: O(n)
	 */
	public int[] keysToArray()
	{
		if (empty()){
			return new int[0];
		}
		int[] keysArr = new int[size()]; 
		IWAVLNode n = min;
		keysArr[0] = n.getKey();
		for (int i=1;i<keysArr.length;i++){
			n = n.successor();
			keysArr[i] = n.getKey();
		}
		return keysArr;              
	}

	/**
	 * public String[] infoToArray()
	 *
	 * Returns an array which contains all info in the tree,
	 * sorted by their respective keys,
	 * or an empty array if the tree is empty.
	 * 
	 * complexity: O(n)
	 */
	public String[] infoToArray()
	{
		if (empty()){
			return new String[0];
		}
		String[] infoArr = new String[size()]; 
		IWAVLNode n = min;
		infoArr[0] = n.getValue();
		for (int i=1;i<infoArr.length;i++){
			n = n.successor();
			infoArr[i] = n.getValue();
		}
		return infoArr;                  
	}
	
	/**
	 * public int size()
	 *
	 * Returns the number of nodes in the tree.
	 *
	 * precondition: none
	 * postcondition: none
	 * 
	 * complexity: O(1)
	 */
	public int size()
	{
		if (root==null)
			return 0;
		return root.getSubtreeSize();
	}

	/**
	 * public IWAVLNode getRoot()
	 *
	 * Returns the root WAVL node, or null if the tree is empty
	 *
	 * precondition: none
	 * postcondition: none
	 * 
	 * complexity: O(1)
	 */
	public IWAVLNode getRoot()
	{
		return root;
	}
	
	/**
	 * public int select(int i)
	 *
	 * Returns the value of the i'th smallest key (return -1 if tree is empty)
	 * Example 1: select(1) returns the value of the node with minimal key 
	 * Example 2: select(size()) returns the value of the node with maximal key 
	 * Example 3: select(2) returns the value 2nd smallest minimal node, i.e the value of the node minimal node's successor 
	 *
	 * precondition: size() >= i > 0
	 * postcondition: none
	 * 
	 * complexity: O(min{log(i),log(n-i)})
	 */   
	public String select(int i) {
		
		if (empty() || i>size() || i<0){
			return null;
		}
		
		IWAVLNode cur;
		int j;
		
		if (i <= size()-i){ //start the search from the minimum node
			cur = min;
			 j = i;
		}
		else { //start the search from the maximum node
			cur = max;
			 j = size()-i+1; //the i'th smallest key is the j'th largest key in the tree
		}
		
		//go up to the node that contains the j'th smallest/largest key in its subtree
		while (cur.getSubtreeSize() < j) {
			cur = cur.getParent();
		}
		
		if (i > size()-i) {
			//the j'the largest key is the (cur.size-j+1)'th smallest key in this subtree
			j = cur.getSubtreeSize()-j+1; 
		}
		
		int curRANK = 1+cur.getLeft().getSubtreeSize(); //smaller elements than the current node
		while (j != curRANK) {	
			if (j < curRANK) { //the j'th smallest key is in the left subtree
				cur = cur.getLeft();
				curRANK -= (cur.getRight().getSubtreeSize()+1);
			}
			else  { //the j'th smallest key is in the right subtree
				cur = cur.getRight();
				curRANK += cur.getLeft().getSubtreeSize()+1;
			}	
		}
		return cur.getValue(); //cur has the j'th smallest key
	}
	

	/**
	 * public interface IWAVLNode
	 * ! Do not delete or modify this - otherwise all tests will fail !
	 */
	public interface IWAVLNode{	
		/**returns node's key (for virtual node return -1)
		 * @Complexity O(1)*/
		public int getKey(); 
		
		/**returns node's value [info] (for virtual node return null)
		 * @Complexity O(1)*/
		public String getValue(); 
		
		/**returns left child (if there is no left child return null)
		 * @Complexity O(1)*/
		public IWAVLNode getLeft();
		
		/**returns right child (if there is no right child return null)
		 * @Complexity O(1)*/
		public IWAVLNode getRight();
		
		/**Returns True if this is a non-virtual WAVL node (i.e not a virtual leaf or a sentinal)
		 * @Complexity O(1)*/
		public boolean isRealNode();
		
		/**Returns the number of real nodes in this node's subtree (Should be implemented in O(1))
		 * @Complexity O(1)*/
		public int getSubtreeSize(); 
		
		/**makes l as the left child of this node
		 * @param l : the node to be set as left child
		 * @pre l!=null
		 * @post this.left==l && l.parent==this
		 * @Complexity O(1)*/
		public void setLeft(IWAVLNode l);
		
		/**makes r as the right child of this node
		 * @param r : the node to be set as right child
		 * @pre r!=null
		 * @post this.right==l && l.right==this
		 * @Complexity O(1)*/
		public void setRight(IWAVLNode r);
		
		/**returns the rank of this node
		 * @Complexity O(1)*/
		public int getRank(); 
		
		/**adds 1 to the rank of this node
		 * @pre none
		 * @post this.rank==$prev(this.rank)+1
		 * @Complexity O(1)*/
		public void promote(); 
		
		/**decreases 1 of the rank of this node
		 * @pre none
		 * @post this.rank==$prev(this.rank)-1
		 * @Complexity O(1)*/
		public void demote(); 
		
		/**returns the parent of this node
		 * @pre none
		 * @post $ret==this.parent
		 * @Complexity O(1)*/
		public IWAVLNode getParent();
		
		/**makes p as this node's parent
		 * @param p : the node to be set as the parent
		 * @pre none
		 * @post this.parent==p
		 * @Complexity O(1)*/
		public void setParent(IWAVLNode p); 
		
		/**makes this node as the child of x's parent instead of x
		 * @pre x!=null
		 * @post this.parent==x.parent ,
		 * 		$prev(x.parent.left)==x => this.parent.left==this , 
		 * 		$prev(x.parent.right)==x => this.parent.right==this
		 * @Complexity O(1)*/
		public void replaceParentWith(IWAVLNode x); 
		
		/**increases by 1 the size of the nodes 
		 * in the path between this node and the root
		 * @Complexity O(logn)*/
		public void incSize();  
		
		/**decreases by 1 the size of the nodes 
		 * in the path between this node and the root
		 * @Complexity O(logn)*/
		public void decSize(); 
		
		/**sets the size of this node's subtree to the appropriate value
		 * @pre this.isRealNode()==true
		 * @post this.size==this.left.size+this.right.size+1
		 * @Complexity O(1)*/
		public void updateSize();
		
		/**replaces the node n by this node
		 *@pre n!=null
		 *@post this.parent==n.parent ,
		 *		$prev(n.parent.left)==n => this.parent.left==this , 
		 * 		$prev(n.parent.right)==n => this.parent.right==this ,
		 * 		this.right==n.right ,
		 * 		this.left==n.left ,
		 * 		this.right.parent==this ,
		 * 		this.left.parent==this ,
		 * 		this.size==n.size ,
		 * 		this.rank==n.rank
		 * @Complexity O(1)*/
		public void replaceNodes(IWAVLNode n); 
		
		/**returns the element with the smallest key in this node's subtree
		 * @Complexity O(logn)*/
		public IWAVLNode min(); 
		
		/**returns if the node is an internal leaf
		 * @Complexity O(1)*/
		public boolean isLeaf(); 
		
		/**returns the successor of the node
		 * @Complexity O(logn)*/
		public IWAVLNode successor();  
	}

	/**
	 * public class WAVLNode
	 *
	 * If you wish to implement classes other than WAVLTree
	 * (for example WAVLNode), do it in this file, not in 
	 * another file.
	 * This class can and must be modified.
	 * (It must implement IWAVLNode)
	 */
	public class WAVLNode implements IWAVLNode{
		private IWAVLNode right;
		private IWAVLNode left;
		private IWAVLNode parent;
		private int key;
		private String value;
		private int rank;
		private int size;
		
		/**constructor for virtual leaf*/
		public WAVLNode (){
			rank = -1;
			key = -1;
			value = null;
			right = null;
			left = null;
			parent = null;
			size  = 0;

		}
		
		/**constructor for internal leaf*/
		public WAVLNode(int key,String value){
			right = external;
			left = external;
			this.key = key;
			this.value = value;
			this.rank = 0;
			this.size = 1;
		}
		
		public int getRank(){
			return rank;
		}
		
		public void promote(){
			rank++;
		}
		
		public void demote(){
			rank--;
		}

		public int getKey(){
			return key; 
		}

		public String getValue(){
			return value; 
		}
		
		public IWAVLNode getRight(){
			return right; 
		}	
		
		public void setRight(IWAVLNode r) {
			right = r;
			r.setParent(this);
		}
		
		public IWAVLNode getLeft() {
			return left;
		}
		
		public void setLeft(IWAVLNode l) {
			left = l;
			l.setParent(this);
		}

		public IWAVLNode getParent(){
			return parent;
		}
		
		public void setParent(IWAVLNode p){
			parent = p;
		}
				
		// Returns True if this is a non-virtual WAVL node (i.e not a virtual leaf or a sentinal)
		public boolean isRealNode() {
			return rank != -1;
		}

		public int getSubtreeSize() {
			return size;
		}

		public void incSize() {
			this.size++;
			if (this.parent!=null) {
				this.parent.incSize();
			}
		}

		public void decSize() {
			this.size--;
			if (this.parent!=null) {
				this.parent.decSize();
			}
		}
		
		public void updateSize() {
			size = 1+getLeft().getSubtreeSize()+getRight().getSubtreeSize();
		}

		/*makes this node as the child of x.parent instead of x*/
		public void replaceParentWith(IWAVLNode x) {
			IWAVLNode p = x.getParent();
			if (p!=null) {
				if (p.getRight()==x) {
					p.setRight(this);
				}
				else {
					p.setLeft(this);
				}
			}
			else { // x is the root of the tree
				root = this;
				this.parent = null;
			}

		}

		/*replaces the node n by this node*/
		public void replaceNodes(IWAVLNode n) {
			this.replaceParentWith(n);
			this.setLeft(n.getLeft());
			this.setRight(n.getRight());
			this.size = (n.getSubtreeSize());
			this.rank = n.getRank();
			
		}
		
		public IWAVLNode min() {
			if (!this.left.isRealNode()){//doesn't have a left child
				return this;
			}
			return this.left.min();
		}
		
		public boolean isLeaf() {
			return right==external && left==external;
		}

		public IWAVLNode successor() {
			if (right!=external){
				return right.min();
			}
			IWAVLNode cur = this;
			IWAVLNode p = parent;
			while (p!=null && cur==p.getRight()){
				cur = p;
				p = cur.getParent();
			}
			return p;
		}
		
	}

}

