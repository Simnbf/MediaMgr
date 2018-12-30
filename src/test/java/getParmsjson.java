import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.junit.Test;

import com.sbf.MediaManager.GetParams;

public class getParmsjson {

	@Test
	public void test() {
		File parmFile = new File("control.json");
		try {
			JSONObject a = GetParams.ReadParams(parmFile);
			System.out.println(a.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
