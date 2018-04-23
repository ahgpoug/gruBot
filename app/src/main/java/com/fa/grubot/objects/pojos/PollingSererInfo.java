package com.fa.grubot.objects.pojos;
                 
public class PollingSererInfo
{
    private String ts;

    private String server;

    private String pts;

    private String key;

    public String getTs ()
    {
        return ts;
    }

    public void setTs (String ts)
    {
        this.ts = ts;
    }

    public String getServer ()
    {
        return server;
    }

    public void setServer (String server)
    {
        this.server = server;
    }

    public String getPts ()
    {
        return pts;
    }

    public void setPts (String pts)
    {
        this.pts = pts;
    }

    public String getKey ()
    {
        return key;
    }

    public void setKey (String key)
    {
        this.key = key;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [ts = "+ts+", server = "+server+", pts = "+pts+", key = "+key+"]";
    }
}

			