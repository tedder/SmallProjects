package net.inervo.playpen;

import net.inervo.playpen.BinarySearchTree.Node;

@SuppressWarnings( "rawtypes" )
class BinarySearchTree {
	public static class Node {
		public Node( Comparable value ) {
			this.value = value;
		}

		public Comparable value;
		public Node left = null;
		public Node right = null;
	}

	public BinarySearchTree() {
		this.root = null;
	}

	public Node root;
}

// parent: 7
// children: null 6
// TODO: handle grandparents/strict equality. Example case:
//      7
//    6   9
//   n 8 n n

@SuppressWarnings( { "unchecked", "rawtypes" } )
class BstUtil {
	public static boolean Verify( BinarySearchTree tree ) {
		return Verify( tree.root, null, null );
	}

	public static boolean Verify( Node node, Comparable minAllowed, Comparable maxAllowed ) {
		System.out.println( "v: " + node.value + " should be between [" + minAllowed + ".." + maxAllowed + "]" );
		// our value should be between minAllowed and maxAllowed, inclusive.
		if ( minAllowed != null && node.value.compareTo( minAllowed ) < 0 ) {
			System.out.println( "minAllowed check failed" );
			return false;
		}

		if ( maxAllowed != null && node.value.compareTo( maxAllowed ) > 0 ) {
			System.out.println( "maxAllowed check failed" );
			return false;
		}

		// is the left side correct?
		if ( node.left != null ) {
			if ( Verify( node.left, minAllowed, node.value ) == false ) {
				System.out.println( "left verify failed" );
				return false;
			}

			// child values
			if ( node.value.compareTo( node.left.value ) < 0 ) {
				System.out.println( "left equality failed, " + node.value + " versus " + node.left.value );
				return false;
			}
		}

		// is the right side correct?
		if ( node.right != null ) {
			if ( Verify( node.right, node.value, maxAllowed ) == false ) {
				System.out.println( "right verify failed" );
				return false;
			}

			// child values
			if ( node.value.compareTo( node.right.value ) > 0 ) {
				System.out.println( "right equality failed, " + node.value + " versus " + node.right.value );
				return false;
			}
		}

		return true;
	}
}

class TestBstUtil {
	public static void main( String[] args ) {

		System.out.println( "This should succeed:" );
		testOne();
		System.out.println( "This should fail:" );
		testTwo();
		System.out.println( "This should succeed:" );
		testThree();
	}

	public static void testOne() {
		BinarySearchTree tree = new BinarySearchTree();
		tree.root = new BinarySearchTree.Node( 100 );
		tree.root.left = new BinarySearchTree.Node( 50 );
		tree.root.right = new BinarySearchTree.Node( 150 );
		tree.root.left.left = new BinarySearchTree.Node( 25 );

		if ( BstUtil.Verify( tree ) ) {
			System.out.println( "Correct!" );
		} else {
			System.out.println( "Fix it!" );
		}
	}

	public static void testTwo() {
		BinarySearchTree tree = new BinarySearchTree();
		tree.root = new BinarySearchTree.Node( 100 );
		tree.root.left = new BinarySearchTree.Node( 50 );
		tree.root.right = new BinarySearchTree.Node( 150 );
		tree.root.left.left = new BinarySearchTree.Node( 25 );
		tree.root.left.left.right = new BinarySearchTree.Node( 102 );

		if ( BstUtil.Verify( tree ) ) {
			System.out.println( "Correct!" );
		} else {
			System.out.println( "Fix it!" );
		}
	}
	
	public static void testThree() {
		BinarySearchTree tree = new BinarySearchTree();
		tree.root = new BinarySearchTree.Node( "m" );
		tree.root.left = new BinarySearchTree.Node( "f" );
		tree.root.right = new BinarySearchTree.Node( "r" );
		tree.root.left.left = new BinarySearchTree.Node( "e" );
		tree.root.left.left.right = new BinarySearchTree.Node( "t" );

		if ( BstUtil.Verify( tree ) ) {
			System.out.println( "Correct!" );
		} else {
			System.out.println( "Fix it!" );
		}
	}
}
