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

    @Test
    void jsonErrorThrows() {
        assertThatThrownBy(() -> SeoulStationResponse.parse("""
                {"subwayStationMaster":{"RESULT":{"CODE":"INFO-200","MESSAGE":"해당하는 데이터가 없습니다."}}}"""))
                .hasMessageContaining("INFO-200");
    }

    @Test
    void xmlAuthErrorThrows() {
        assertThatThrownBy(() -> SeoulStationResponse.parse(
                "<RESULT><CODE>INFO-100</CODE><MESSAGE><![CDATA[인증키가 유효하지 않습니다.]]></MESSAGE></RESULT>"))
                .hasMessageContaining("INFO-100");
    }
}
