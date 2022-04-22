/*******************************************************************************
 * Copyright (c) 2012 CGI Federal.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  CGI Federal - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.core.archive.compound.v3;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedMap;

/**
 * Given sorted map keyed by {@link String}, this provides an {@link Iterable}
 * for iterating over the keys starting with the given prefix.
 */
public class PrefixedIterable implements Iterable<String>
{
	private final String prefix;
	private final Iterable<String> strings;

	/**
	 * @param entries source sorted set of entries
	 * @param prefix prefix to filter by
	 * @return an {@link Iterable} of {@link String}s with the specified prefix
	 */
	static Iterable<String> filteredByPrefix(SortedMap<String, ?> entries, String prefix)
	{
		return new PrefixedIterable(entries.tailMap(prefix).keySet(), prefix);
	}

	PrefixedIterable(Iterable<String> strings, String prefix)
	{
		this.prefix = checkNotNull(prefix);
		this.strings = checkNotNull(strings);
	}

	public Iterator<String> iterator()
	{
		return new PrefixedIterator(this.strings.iterator(), this.prefix);
	}

	static <T> T checkNotNull(T value)
	{
		if (value == null)
		{
			throw new NullPointerException();
		}
		return value;
	}

	private static class PrefixedIterator implements Iterator<String>
	{
		final String prefix;
		final Iterator<String> strings;
		String next;

		PrefixedIterator(Iterator<String> strings, String prefix)
		{
			this.prefix = prefix;
			this.strings = strings;
			this.next = advance();
		}

		public boolean hasNext()
		{
			return (this.next != null) && this.next.startsWith(this.prefix);
		}

		public String next()
		{
			if (hasNext())
			{
				String current = this.next;
				this.next = advance();
				return current;
			}
			throw new NoSuchElementException();
		}

		private String advance()
		{
			String peek = (this.strings.hasNext()) ? this.strings.next() : null;
			return ((peek != null) && peek.startsWith(this.prefix)) ?  peek : null;
		}

		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
}

