package com.tinkerpop.gremlin.redis.structure;

import com.tinkerpop.gremlin.process.T;
import com.tinkerpop.gremlin.structure.Direction;
import com.tinkerpop.gremlin.structure.Edge;
import com.tinkerpop.gremlin.structure.Element;
import com.tinkerpop.gremlin.structure.Property;
import com.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.gremlin.structure.util.ElementHelper;
import com.tinkerpop.gremlin.structure.util.StringFactory;
import com.tinkerpop.gremlin.util.iterator.IteratorUtils;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class RedisEdge extends RedisElement implements Edge, Edge.Iterators {

    protected final Vertex inVertex; /* source vertex */
    protected final Vertex outVertex; /* destination vertex */
    /* properties to add */
    protected final Object gid; /* this type errp */
    protected String label;


    protected RedisEdge(final Object id, final Vertex outVertex, final String label, final Vertex inVertex, final RedisGraph graph) {
        super(id, label, graph);
        this.outVertex = outVertex;
        this.inVertex = inVertex;
        this.label = label;
        this.gid = id;

        try {
            this.graph.getDatabase().set("edge:"+ String.valueOf(id) + ":out", String.valueOf(out.getId()));
            this.graph.getDatabase().set("edge:"+ String.valueOf(id) + ":in", String.valueOf(in.getId()));
            this.graph.getDatabase().set("edge:"+ String.valueOf(id) + ":label", label);

            this.graph.getDatabase().zadd("vertex:" + String.valueOf(in.getId()) + ":edges:in", id, String.valueOf(id));
            this.graph.getDatabase().zadd("vertex:" + String.valueOf(out.getId()) + ":edges:out", id, String.valueOf(id));

            this.graph.getDatabase().set("edge:" + String.valueOf(id), String.valueOf(id));
            this.graph.getDatabase().zadd("globals:edges", id, String.valueOf(id));

            for (RedisAutomaticIndex index : this.graph.getAutoIndices()) {
                index.autoUpdate(AutomaticIndex.LABEL, this.label, null, this);
            }

        } catch(RedisException e) {
            e.printStackTrace();
        }
    }


    public Vertex getOutVertex() {
        return this.outVertex;
    }

    public Vertex getInVertex() {
        return this.inVertex;
    }

    public String getLabel() {
        return label;
    }


    @Override
    public <V> Property<V> property(final String key, final V value) {
        ElementHelper.validateProperty(key, value);
        final Property oldProperty = super.property(key);
        final Property<V> newProperty = new RedisProperty<>(this, key, value);
        this.properties.put(key, Collections.singletonList(newProperty));
        return newProperty;
    }

    @Override
    public void remove() {
        if (this.removed)
            throw Element.Exceptions.elementAlreadyRemoved(Edge.class, this.id);
        final RedisVertex outVertex = (RedisVertex) this.outVertex;
        final RedisVertex inVertex = (RedisVertex) this.inVertex;

        if (null != outVertex && null != outVertex.outEdges) {
            final Set<Edge> edges = outVertex.outEdges.get(this.label());
            if (null != edges)
                edges.remove(this);
        }
        if (null != inVertex && null != inVertex.inEdges) {
            final Set<Edge> edges = inVertex.inEdges.get(this.label());
            if (null != edges)
                edges.remove(this);
        }

        this.graph.edges.remove(this.id());
        this.properties.clear();
        this.removed = true;
    }

    @Override
    public String toString() {
        return StringFactory.edgeString(this);

    }

    //////////////////////////////////////////////

    @Override
    public Edge.Iterators iterators() {
        return this;
    }

    @Override
    public Iterator<Vertex> vertexIterator(final Direction direction) {
        switch (direction) {
            case OUT:
                return IteratorUtils.of(this.outVertex);
            case IN:
                return IteratorUtils.of(this.inVertex);
            default:
                return IteratorUtils.of(this.outVertex, this.inVertex);
        }
    }

    @Override
    public <V> Iterator<Property<V>> propertyIterator(final String... propertyKeys) {
        return (Iterator) super.propertyIterator(propertyKeys);
    }
}
