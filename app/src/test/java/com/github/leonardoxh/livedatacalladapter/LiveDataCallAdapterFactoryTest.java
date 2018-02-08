package com.github.leonardoxh.livedatacalladapter;

import android.arch.lifecycle.LiveData;

import com.google.common.reflect.TypeToken;

import org.junit.Before;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.mockwebserver.MockWebServer;
import retrofit2.CallAdapter;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

public class LiveDataCallAdapterFactoryTest {
    private static final Annotation[] NO_ANNOTATIONS = new Annotation[0];

    private final MockWebServer server = new MockWebServer();
    private final CallAdapter.Factory factory = LiveDataCallAdapterFactory.create();
    private Retrofit retrofit;

    @Before
    public void setUp() {
        retrofit = new Retrofit.Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(new StringConverterFactory())
                .addCallAdapterFactory(factory)
                .build();
    }

    @Test
    public void responseType() {
        Type bodyClass = new TypeToken<LiveData<Resource<String>>>() {}.getType();
        assertThat(factory.get(bodyClass, NO_ANNOTATIONS, retrofit).responseType())
                .isEqualTo(new TypeToken<Resource<String>>() {}.getType());

        Type bodyWildcard = new TypeToken<LiveData<Resource<? extends String>>>() {}.getType();
        assertThat(factory.get(bodyWildcard, NO_ANNOTATIONS, retrofit).responseType())
                .isEqualTo(new TypeToken<Resource<? extends String>>() {}.getType());

        Type responseType = new TypeToken<LiveData<Response<Resource<String>>>>() {}.getType();
        assertThat(factory.get(responseType, NO_ANNOTATIONS, retrofit).responseType())
                .isEqualTo(new TypeToken<Response<Resource<String>>>() {}.getType());

        Type responseTypeWildcard = new TypeToken<LiveData<Response<Resource<? extends String>>>>() {}.getType();
        assertThat(factory.get(responseTypeWildcard, NO_ANNOTATIONS, retrofit).responseType())
                .isEqualTo(new TypeToken<Response<Resource<? extends String>>>() {}.getType());
    }

    @Test
    public void nonListenableFutureReturnsNull() {
        CallAdapter<?, ?> adapter = factory.get(String.class, NO_ANNOTATIONS, retrofit);
        assertThat(adapter).isNull();
    }

    @Test
    public void rawTypesThrows() {
        Type liveDataType = new TypeToken<LiveData>() {}.getType();
        try {
            CallAdapter callAdapter = factory.get(liveDataType, NO_ANNOTATIONS, retrofit);
            fail("Unespected callAdapter = " + callAdapter.getClass().getName());
        } catch (IllegalStateException e) {
            assertThat(e).hasMessageThat().isEqualTo("Response must be parametrized as " +
                    "Response<LiveData> or Response<? extends LiveData>");
        }
    }

    @Test
    public void rawResponseTypesThrows() {
        Type liveDataType = new TypeToken<LiveData<Response>>() {}.getType();
        try {
            CallAdapter callAdapter = factory.get(liveDataType, NO_ANNOTATIONS, retrofit);
            fail("Unespected callAdapter = " + callAdapter.getClass().getName());
        } catch (IllegalStateException e) {
            assertThat(e).hasMessageThat().isEqualTo("Response must be parametrized as " +
                    "Response<LiveData> or Response<? extends LiveData>");
        }
    }
}