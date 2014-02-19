package be.multec.sg.nodes.controllers;

import be.multec.sg.nodes.SGNode;

/**
 * Basic adapter of the INodeController interface that provides an empty implementation of the added
 * method.
 * 
 * @author Wouter Van den Broeck
 */
public abstract class NodeController implements INodeController {
	
	/* @see be.multec.sg.nodeControllers.INodeController#added(be.multec.sg.SGNode) */
	@Override
	public void added(SGNode node) {}
	
}
