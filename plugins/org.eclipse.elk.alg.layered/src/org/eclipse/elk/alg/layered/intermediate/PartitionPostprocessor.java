/*******************************************************************************
 * Copyright (c) 2016 Kiel University and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Kiel University - initial API and implementation
 *******************************************************************************/
package org.eclipse.elk.alg.layered.intermediate;

import java.util.Iterator;

import org.eclipse.elk.alg.layered.graph.LGraph;
import org.eclipse.elk.alg.layered.graph.LNode;
import org.eclipse.elk.alg.layered.graph.LPort;
import org.eclipse.elk.alg.layered.graph.Layer;
import org.eclipse.elk.alg.layered.options.InternalProperties;
import org.eclipse.elk.core.alg.ILayoutProcessor;
import org.eclipse.elk.core.util.IElkProgressMonitor;


/**
 * Remove all edges added by the {@link PartitionPreprocessor}.
 *
 * <dl>
 *   <dt>Precondition:</dt>
 *     <dd>a layered graph.</dd>
 *   <dt>Postcondition:</dt>
 *     <dd>layered graph with all partition constraint edges removed.</dd>
 *   <dt>Slots:</dt>
 *     <dd>Before phase 3.</dd>
 *   <dt>Same-slot dependencies:</dt>
 *     <dd>None.</dd>
 * </dl>
 *
 * @see PartitionPreprocessor
 */
public class PartitionPostprocessor implements ILayoutProcessor<LGraph> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(final LGraph lGraph, final IElkProgressMonitor monitor) {
        monitor.begin("Removing partition constraint edges", 1);
        // Remove all added ports and edges.
        for (Layer layer : lGraph) {
            for (LNode node : layer) {
                final Iterator<LPort> ports = node.getPorts().iterator();
                while (ports.hasNext()) {
                    LPort port = ports.next();
                    if (port.getProperty(InternalProperties.PARTITION_DUMMY)) {
                        // Remove the port explicitly from the iterator to prevent
                        // ConcurrentModificationExceptions. This removes the port from the underlying
                        // collection and thus from the node's list of ports.
                        ports.remove();
                    }
                }
            }
        }
        monitor.done();
    }

}
