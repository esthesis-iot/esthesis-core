package esthesis.service.common.paging;

import io.quarkus.panache.common.Sort;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for Pageable, testing pagination functionality.
 */
class PageableTest {
	private Pageable pageable;
	private UriInfo uriInfo;

	@BeforeEach
	void setup() {
		pageable = new Pageable();
		uriInfo = mock(UriInfo.class);
		pageable.setUriInfo(uriInfo);
	}

	@Test
	void testGetPageObjectPresent() {
		pageable.setPage(1);
		pageable.setSize(20);
		Optional<io.quarkus.panache.common.Page> page = pageable.getPageObject();
		assertTrue(page.isPresent());
		assertEquals(1, page.get().index);
		assertEquals(20, page.get().size);
	}

	@Test
	void testGetPageObjectEmpty() {
		assertTrue(pageable.getPageObject().isEmpty());
	}

	@Test
	void testGetSortObjectEmpty() {
		pageable.setSort("");
		Sort sort = pageable.getSortObject();
		assertTrue(sort.getColumns().isEmpty());
	}

	@Test
	void testGetSortObjectValid() {
		pageable.setSort("name,asc,id,desc");
		Sort sort = pageable.getSortObject();
		assertNotNull(sort);
	}

	@Test
	void testGetSortObjectInvalidLength() {
		pageable.setSort("name,asc,id");
		assertThrows(IllegalArgumentException.class, pageable::getSortObject);
	}

	@Test
	void testGetSortObjectInvalidDirection() {
		pageable.setSort("name,ascending");
		assertThrows(IllegalArgumentException.class, pageable::getSortObject);
	}

	@Test
	void testHasQueryTrue() {
		MultivaluedMap<String, String> map = new MultivaluedHashMap<>();
		map.add("custom", "value");
		when(uriInfo.getQueryParameters()).thenReturn(map);
		assertTrue(pageable.hasQuery());
	}

	@Test
	void testHasQueryFalse() {
		MultivaluedMap<String, String> map = new MultivaluedHashMap<>();
		map.add("page", "1");
		when(uriInfo.getQueryParameters()).thenReturn(map);
		assertFalse(pageable.hasQuery());
	}

	@Test
	void testGetQueryKeysVariousTypes() {
		MultivaluedMap<String, String> map = new MultivaluedHashMap<>();
		map.add("name*", "john");
		map.add("age>", "30");
		map.add("active", "true");
		map.add("roles[]", "[admin|user]");
		when(uriInfo.getQueryParameters()).thenReturn(map);

		String result = pageable.getQueryKeys();
		assertTrue(result.contains("name like"));
		assertTrue(result.contains("age >"));
		assertTrue(result.contains("roles in"));
	}

	@Test
	void testGetQueryValuesWithVariousTypes() {
		MultivaluedMap<String, String> map = new MultivaluedHashMap<>();
		map.add("text", "'some text'");
		map.add("flag", "true");
		map.add("count", "42");
		map.add("timestamp", Instant.now().toString());
		map.add("list[]", "[a|b|c]");
		when(uriInfo.getQueryParameters()).thenReturn(map);

		Map<String, Object> values = pageable.getQueryValues();
		assertEquals("some text", values.get("v_" + String.valueOf("text".hashCode()).replace("-", "")));
		assertEquals(true, values.get("v_" + String.valueOf("flag".hashCode()).replace("-", "")));
		assertEquals(42, values.get("v_" + String.valueOf("count".hashCode()).replace("-", "")));
		assertInstanceOf(Instant.class, values.get("v_" + String.valueOf("timestamp".hashCode()).replace("-", "")));
		assertEquals("a,b,c", values.get("v_" + String.valueOf("list[]".hashCode()).replace("-", "")));
	}

	@Test
	void testToString() {
		when(uriInfo.getRequestUri()).thenReturn(URI.create("http://localhost/api/items?page=1&size=10"));
		pageable.setPage(1).setSize(10).setSort("id,asc");
		String result = pageable.toString();
		assertTrue(result.contains("page=1"));
		assertTrue(result.contains("size=10"));
		assertTrue(result.contains("sort=id,asc"));
	}

}
