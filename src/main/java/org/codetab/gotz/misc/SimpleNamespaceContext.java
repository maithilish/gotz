package org.codetab.gotz.misc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

public class SimpleNamespaceContext implements NamespaceContext {

    private static final String DEFAULT_NS = "default"; //$NON-NLS-1$
    private Map<String, String> prefixMap = new HashMap<>();

    public SimpleNamespaceContext() {
    }

    public SimpleNamespaceContext(final String prefix, final String uri) {
        if (prefix == null || prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
            prefixMap.put(DEFAULT_NS, uri);
        } else {
            prefixMap.put(prefix, uri);
        }
    }

    public void addPrefixMapping(final String prefix, final String uri) {
        prefixMap.put(prefix, uri);
    }

    @Override
    public String getNamespaceURI(final String prefix) {
        if (prefix == null || prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
            return prefixMap.get(DEFAULT_NS);
        } else {
            return prefixMap.get(prefix);
        }
    }

    @Override
    public String getPrefix(final String namespaceURI) {
        return null;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Iterator getPrefixes(final String namespaceURI) {
        return null;
    }
}
