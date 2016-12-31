package com.terramaster.ftp;

import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;

import java.io.IOException;

/**
 * Created by Jaylin on 2016/12/15.
 */

public class TerCls implements CopyStreamListener {

    public long stotalBytesTransferred;
    public  int sbytesTransferred;
    public  long sstreamSize;

    TerCls() {
        stotalBytesTransferred =0;
        sbytesTransferred = 0;
        sstreamSize =0;
    }

    @Override
    public void bytesTransferred(CopyStreamEvent event) {
        bytesTransferred(event.getTotalBytesTransferred(), event.getBytesTransferred(), event.getStreamSize());
    }

    @Override
    public void bytesTransferred(long totalBytesTransferred,
                                 int bytesTransferred, long streamSize) {

        this.stotalBytesTransferred =totalBytesTransferred;
        sbytesTransferred = bytesTransferred;
        sstreamSize =streamSize;
        //    Log.e("listen","stotalBytesTransferred"+stotalBytesTransferred);
    }

    void cleanUp() throws IOException {

    }

}
