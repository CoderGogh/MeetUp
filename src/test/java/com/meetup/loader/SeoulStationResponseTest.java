package com.meetup.loader;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeoulStationResponseTest {

    @Test
    void parsesRows() {
        var rows = SeoulStationResponse.parse("""
                {"subwayStationMaster":{"list_total_count":1,"RESULT":{"CODE":"INFO-000"},
                 "row":[{"BLDN_ID":"0150","BLDN_NM":"서울역","ROUTE":"1호선","LAT":"37.556228","LOT":"126.972135"}]}}""");
        assertThat(rows).containsExactly(new MasterStation("서울역", "1호선", 37.556228, 126.972135));
    }
}
