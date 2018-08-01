/*
 * Copyright (c) 2003-2005 The BISON Project
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 2 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */
		
package peersim.core;

import peersim.graph.Graph;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;

/**
* This class is an adaptor which makes a {@link Linkable} protocol layer
* look like a graph. It is useful because it allows the application of many
* graph algorithms and graph topology initialization methods.
* If the overlay network changes after creating this object, the changes
* will be reflected. However, if the nodes are reshuffled (see
* {@link Network#shuffle}), or if the node list changes (addition/removal),
* then the behaviour becomes unspecified.
*
* The indices of nodes are from 0 to Network.size()-1.
*
* The fail state of nodes has an effect on the graph: all nodes are included
* but edges are included only if both ends are up. This expresses the fact
* that this graph is in fact defined by the "can communicate with" relation.
*/

/**
 * Modified by Hong on 2017/6/28.
 * 由于原先的OverlayGraph没有实现clearEdge方法，因此没法实现动态模拟
 * 为此，对OverlayGraph进行一定程度的改写，改用MyLinkable协议来进行相关操作
 */

public class OverlayGraph implements Graph {


// ====================== fields ================================
// ==============================================================

/**
* The protocol ID that selects the Linkable protocol to convert to a graph.
*/
public final int protocolID;

/**
* Tells if the graph should be wired in an undirected way.
* Method {@link #directed} returns true always, this affects only
* method {@link #setEdge}: if false, then the opposite edge is set too.
*/
public final boolean wireDirected;

// ====================== public constructors ===================
// ==============================================================

/**
* @param protocolID The protocol on which this adaptor is supposed
* to operate.
*/
public OverlayGraph( int protocolID ) {

	this.protocolID = protocolID;
	wireDirected = false; // 默认为无向图
}

// --------------------------------------------------------------

/**
* @param protocolID The protocol on which this adaptor is supposed
* to operate.
* @param wireDirected specifies if {@link #setEdge} would wire the
* opposite edge too.
*/
public OverlayGraph( int protocolID, boolean wireDirected ) {

	this.protocolID = protocolID;
	this.wireDirected = wireDirected;
}


// ======================= Graph implementations ================
// ==============================================================


public boolean isEdge(int i, int j) {
	
	return
		((MyLinkable)Network.node[i].getProtocol(protocolID)
		).contains(Network.node[j]) &&
		Network.node[j].isUp() &&
		Network.node[i].isUp();
}

// ---------------------------------------------------------------

/**
* Returns those neighbors that are up. If node i is not up, it returns
* an empty list.
*/
public Collection<Integer> getNeighbours(int i) {
	
	MyLinkable myLinkable=(MyLinkable)Network.node[i].getProtocol(protocolID);
	ArrayList<Integer> al = new ArrayList<>(myLinkable.degree());
	if( Network.node[i].isUp() )
	{	
		for(int j=0; j<myLinkable.degree(); ++j)
		{
			final Node n = myLinkable.getNeighbor(j);
			// if accessible, we include it
			if(n.isUp()) al.add(Integer.valueOf(n.getIndex()));
		}
	}
	return Collections.unmodifiableList(al);
}

// ---------------------------------------------------------------

/** Returns <code>Network.node[i]</code> */
public Object getNode(int i) { return Network.node[i]; }
	
// ---------------------------------------------------------------

/**
* Returns null always
*/
public Object getEdge(int i, int j) { return null; }

// ---------------------------------------------------------------

/** Returns <code>Network.size()</code> */
public int size() { return Network.size(); }

// --------------------------------------------------------------------
	
/** Returns always false */
public boolean directed() { return false; }

// --------------------------------------------------------------------

/**
* Sets given edge.
* In some cases this behaves strangely. Namely, when node i or j is not up,
* but is not dead (e.g. it can be down temporarily).
* In such situations the relevant link is made, but afterwards
* getEdge(i,j) will NOT return true, only when the fail state has changed back
* to OK.
*
* <p>Conceptually one can think of it as a successful operation which is
* immediately overruled by the dynamics of the underlying overlay network.
* Let's not forget that this class is an adaptor only.
*
* <p>
* The behaviour of this method is affected by parameter {@link #wireDirected}.
* If it is false, then the opposite edge is set too.
*/
public boolean setEdge( int i, int j ) {
// XXX slightly unintuitive behavior but makes sense when understood
	
	if( !wireDirected ) 
		((MyLinkable)Network.node[j].getProtocol(protocolID)
		).addNeighbor(Network.node[i]);


	return
		((MyLinkable)Network.node[i].getProtocol(protocolID)
		).addNeighbor(Network.node[j]);
}

// ---------------------------------------------------------------

/** Modified by Hong on 2017/6/28 */
public boolean clearEdge( int i, int j ) {
	
	if (!wireDirected){
		((MyLinkable)Network.node[j].getProtocol(protocolID)
		).removeNeighbor(Network.node[i]);
	}


	return ((MyLinkable)Network.node[i].getProtocol(protocolID)
	).removeNeighbor(Network.node[j]);
}

// ---------------------------------------------------------------

/**
* Returns number of neighbors that are up. If node i is down, returns 0.
*/
public int degree(int i) {

	if( !Network.node[i].isUp() ) return 0;
	MyLinkable myLinkable=(MyLinkable) Network.node[i].getProtocol(protocolID);
	int numNeighbours = 0;
	for(int j=0; j<myLinkable.degree(); ++j)
	{
		final Node n = myLinkable.getNeighbor(j);
		// if accessible, we count it
		if(n.isUp()) numNeighbours++;
	}
	return numNeighbours;
}


// ========================= other methods =======================
// ===============================================================


/**
* Returns number of neighbors that are either up or down.
* If node i is down, returns 0.
*/
public int fullDegree(int i) {

	if( !Network.node[i].isUp() ) return 0;
	MyLinkable myLinkable=(MyLinkable)Network.node[i].getProtocol(protocolID);
	return myLinkable.degree();
}


}


