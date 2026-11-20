package com.meetup.loader;

import com.meetup.place.PlaceRepository;
import com.meetup.subway.GraphParams;
import org.springframework.beans.factory.annotation.Autowired;

class GraphMatrixLoaderTest extends LoaderDbTest {

    @Autowired
    PlaceRepository places;
    @Autowired
    TravelTimeWriter writer;
    @Autowired
    OdsaySampleRepository samples;

    GraphReport run() {
        return new GraphMatrixLoader(places, writer, samples, GraphParams.DEFAULT).run();
    }
}
