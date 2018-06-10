package org.codetab.gotz.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.HashMap;
import java.util.Map;

import org.codetab.gotz.metrics.MetricsHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.codahale.metrics.Counter;

public class ParserCacheTest {

    @Mock
    private MetricsHelper metricsHelper;

    @InjectMocks
    private ParserCache cache;

    private Counter hit;
    private Counter miss;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        hit = new Counter();
        miss = new Counter();
    }

    @Test
    public void testGet() {
        given(metricsHelper.getCounter(cache, "parser", "cache", "hit"))
                .willReturn(hit);
        given(metricsHelper.getCounter(cache, "parser", "cache", "miss"))
                .willReturn(miss);

        cache.put(1, "test");

        String result = cache.get(1);

        assertThat(result).isEqualTo("test");
        assertThat(hit.getCount()).isEqualTo(1);
        assertThat(miss.getCount()).isEqualTo(0);
    }

    @Test
    public void testGetMiss() {
        given(metricsHelper.getCounter(cache, "parser", "cache", "hit"))
                .willReturn(hit);
        given(metricsHelper.getCounter(cache, "parser", "cache", "miss"))
                .willReturn(miss);

        String result = cache.get(1);

        assertThat(result).isNull();
        assertThat(hit.getCount()).isEqualTo(0);
        assertThat(miss.getCount()).isEqualTo(1);
    }

    @Test
    public void testPut() {
        given(metricsHelper.getCounter(cache, "parser", "cache", "hit"))
                .willReturn(hit);
        given(metricsHelper.getCounter(cache, "parser", "cache", "miss"))
                .willReturn(miss);

        cache.put(1, "test");
        assertThat(cache.get(1)).isEqualTo("test");

        cache.put(2, null);
        assertThat(cache.get(2)).isNull();
    }

    @Test
    public void testGetKey() {
        Map<String, String> map = new HashMap<>();
        map.put("region", "div > p ");
        map.put("field", "table > tr");

        int result = cache.getKey(map);

        assertThat(result).isEqualTo(-953150896);
    }

}
