// Copyright (c) 2009, Richard Kennard
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
// * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
// * Redistributions in binary form must reproduce the above copyright
// notice, this list of conditions and the following disclaimer in the
// documentation and/or other materials provided with the distribution.
// * Neither the name of Richard Kennard nor the
// names of its contributors may be used to endorse or promote products
// derived from this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY RICHARD KENNARD ''AS IS'' AND ANY
// EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL RICHARD KENNARD BE LIABLE FOR ANY
// DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
package ste.web.http;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.fail;

import static org.assertj.core.api.BDDAssertions.then;
import org.junit.Test;

import ste.web.http.QueryString.Separator;

/**
 * Unit tests for QueryString
 *
 * @author Richard Kennard
 * @version 1.2
 */
public class BugFreeQueryString {

    /**
     * Test getters
     */
    @Test
    public void getters() {

        QueryString queryString = QueryString.parse("id=1");

        then(queryString.toString()).isEqualTo("id=1");
        then(queryString.toString()).isEqualTo("id=1");

        queryString = QueryString.parse("x=1&y=2");

        then(queryString.toString()).isEqualTo("x=1&y=2");
        then("x=1;y=2").isEqualTo(queryString.toString(Separator.SEMICOLON));
        then(queryString.get("x")).isEqualTo("1");
        then(queryString.getValues("y").get(0)).isEqualTo("2");
        then(queryString.getMap().get("y").get(0)).isEqualTo("2");
        then(queryString.get("z")).isNull();
        then(!queryString.contains("z"));

        then(
            queryString.getNames()
        ).hasSize(2).contains("x").contains("y");
        
	// contains
        queryString = QueryString.parse("x=1&y=2&z");
        then(queryString.get("z")).isNull();
        then(queryString.contains("z")).isTrue();
    }

    /**
     * Test setters
     */
    @Test
    public void setters()
            throws URISyntaxException {

		// New parameter
        QueryString queryString = QueryString.create();
        queryString.set("forumId", 3);

        then(queryString.toString()).isEqualTo("forumId=3");

        queryString.set("forumId", (Number) null);

        then(queryString.toString()).isEmpty();

        try {
            queryString.set(null, "3");
            fail("missing check for not nullable parameters");
        } catch (NullPointerException e) {
            // Should fail
        }

        try {
            queryString.set(null, (String) null);
            fail("missing check for not nullable parameters");
        } catch (NullPointerException e) {
            // Should fail
        }

        queryString.set("name", "Richard Kennard");

        then(queryString.toString()).isEqualTo("name=Richard+Kennard");

        queryString.append("name", "Julianne Kennard");

        then(queryString.toString()).isEqualTo("name=Richard+Kennard&name=Julianne+Kennard");

        queryString.append("name", (String) null).append(null);

        then(queryString.toString()).isEqualTo("name=Richard+Kennard&name=Julianne+Kennard&name");

        queryString.append("name=Charlotte+Kennard&name=Millie+Kennard");

        then(queryString.toString()).isEqualTo("name=Richard+Kennard&name=Julianne+Kennard&name&name=Charlotte+Kennard&name=Millie+Kennard");

        queryString.set("name=Charlotte+Kennard;name=Millie+Kennard;add");

        then(queryString.toString()).isEqualTo("name=Charlotte+Kennard&name=Millie+Kennard&add");

        queryString.remove("name");

        then(queryString.toString()).isEqualTo("add");

        then(queryString.isEmpty()).isFalse();
        queryString.remove("add");
        then(queryString.isEmpty()).isTrue();

        queryString = QueryString.parse(new URI("http://java.sun.com?a=%3C%3E%26&b=2"));

        then(queryString.get("a")).isEqualTo("<>&");

        Map<String, List<String>> queryMap = queryString.getMap();
        queryMap.get("a").add(0, "foo");
        queryMap.put("b", new ArrayList<String>(Arrays.asList("3")));

		// (should not have modified original)
        then(queryString.toString()).isEqualTo("a=%3C%3E%26&b=2");

        queryString = QueryString.create(queryString.getMap());

        then(queryString.toString()).isEqualTo("a=%3C%3E%26&b=2");

        queryMap.get("a").add(0, "foo");
        then(queryString.toString()).isEqualTo("a=%3C%3E%26&b=2");

		// Test round-trip
        queryString = QueryString.create();
        queryString.set("a", "x&y");
        queryString.set("b", "u;v");

        then(queryString.toString()).isEqualTo("a=x%26y&b=u%3Bv");

        queryString = QueryString.parse(queryString.toString());
        then(queryString.get("a")).isEqualTo("x&y");
        then(queryString.get("b")).isEqualTo("u;v");
    }

    /**
     * Test apply
     */
    @Test
    public void apply()
            throws URISyntaxException {

        URI uri = new URI("http://java.sun.com?page=1");
        QueryString queryString = QueryString.parse(uri);
        queryString.set("page", 2);
        uri = queryString.apply(uri);

        then(uri.toString()).isEqualTo("http://java.sun.com?page=2");

        uri = new URI("/forum.jsp?message=12");
        queryString = QueryString.parse(uri).append("reply", 2);
        uri = queryString.apply(uri);

        then(uri.toString()).isEqualTo("/forum.jsp?message=12&reply=2");

		// Test escaping
        uri = new URI("http://www.google.com/search?q=foo+bar");
        queryString = QueryString.parse(uri);
        queryString.set("q", "100%");
        uri = queryString.apply(uri);
        then(uri.toString()).isEqualTo("http://www.google.com/search?q=100%25");

        queryString.append("%", "%25");
        uri = queryString.apply(uri);
        then(uri.toString()).isEqualTo("http://www.google.com/search?q=100%25&%25=%2525");

        queryString.set("q", "a + b = 100%");
        queryString.remove("%");
        uri = queryString.apply(uri);
        then(uri.toString()).isEqualTo("http://www.google.com/search?q=a+%2B+b+%3D+100%25");

		// Test different parts of the URI
        uri = new URI("http://rkennard@java.sun.com:80#bar");
        uri = queryString.apply(uri);
        then(uri.toString()).isEqualTo("http://rkennard@java.sun.com:80?q=a+%2B+b+%3D+100%25#bar");

        uri = new URI("http", "userinfo", "::192.9.5.5", 8080, "/path", "query", "fragment");
        uri = queryString.apply(uri);
        then(uri.toString()).isEqualTo("http://userinfo@[::192.9.5.5]:8080/path?q=a+%2B+b+%3D+100%25#fragment");

        uri = new URI("http", "userinfo", "[::192.9.5.5]", 8080, "/path", "query", "fragment");
        uri = queryString.apply(uri);
        then(uri.toString()).isEqualTo("http://userinfo@[::192.9.5.5]:8080/path?q=a+%2B+b+%3D+100%25#fragment");

        uri = new URI("file", "/authority", null, null, null);
        uri = queryString.apply(uri);
        then(uri.toString()).isEqualTo("file:///authority?q=a+%2B+b+%3D+100%25");
    }

    /**
     * Test equals
     */
    @Test
    public void testEquals()
            throws Exception {

        URI uri = new URI("http://java.sun.com?page=1&para=2");
        QueryString queryString = QueryString.parse(uri);
        then(queryString).isEqualTo(queryString);
        then(queryString.equals(uri)).isFalse();

        QueryString queryString2 = QueryString.create();
        then(queryString.equals(queryString2)).isFalse();
        then(queryString2.equals(queryString)).isFalse();

        queryString2 = QueryString.parse(uri.getQuery());
        then(queryString2).isEqualTo(queryString);

        then(queryString2.hashCode()).isEqualTo(queryString.hashCode());

        queryString.set("page", 2);
        then(queryString.equals(queryString2)).isFalse();
        then(queryString.hashCode() != queryString2.hashCode()).isTrue();

        queryString = QueryString.create();
        queryString2 = QueryString.create();

        then(queryString2).isEqualTo(queryString);
        then(queryString2.hashCode()).isEqualTo(queryString.hashCode());
    }

    /**
     * Test round-trip
     */
    @Test
    public void testRoundTrip()
            throws Exception {

        then(QueryString.parse("page=1&para=2").toString()).isEqualTo("page=1&para=2");
        then(QueryString.parse("bar=&baz").toString()).isEqualTo("bar=&baz");
        then(QueryString.parse("bar=1&bar=2&bar&bar=&bar=3").toString()).isEqualTo("bar=1&bar=2&bar&bar=&bar=3");
    }

    @Test
    public void testUrlEncodedParameterNames() {
        then(QueryString.parse("%70age=1&par%61=2").toString()).isEqualTo("page=1&para=2");
    }
    
    @Test
    public void getNamesAsSet() {
        then(
            QueryString.parse("").getNames()
        ).isNotNull().isEmpty();
        then(
            QueryString.parse("page=1&para=2").getNames()
        ).isNotNull().isNotEmpty().contains("page").contains("para");
    }
}
