package connector;

import com.google.gson.JsonElement;

public interface DataListener
{
    public void dataReceived(JsonElement data);
}
