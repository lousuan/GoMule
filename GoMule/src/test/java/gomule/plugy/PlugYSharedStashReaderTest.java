package gomule.plugy;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import randall.d2files.D2TxtFile;

import java.net.URL;

class PlugYSharedStashReaderTest {
    @BeforeEach
    public void setup() {
        D2TxtFile.constructTxtFiles("./d2111");
    }

    @Test
    void testRead() {
        URL url = this.getClass().getResource("/1_13/_LOD_SharedStashSave.sss");
        Assertions.assertNotNull(url);
        PlugYSharedStash plugYSharedStash = PlugYSharedStashReader.read(url.getPath());
        Assertions.assertEquals("02", plugYSharedStash.getFileVersion());
        Assertions.assertEquals(26283667, plugYSharedStash.getSharedGolds());
        Assertions.assertEquals(103, plugYSharedStash.getNumberOfPages());

        PlugYStashPage page = plugYSharedStash.getPage(1);
        Assertions.assertEquals(87, page.getNumberOfItem());
    }
}