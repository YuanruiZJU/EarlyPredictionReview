package cs.zju.utils;

import java.io.IOException;
import java.nio.charset.Charset;
import com.csvreader.CsvWriter;

public class ResultToCSV {
	private String[] header;
	private String path;
	
	private CsvWriter wr;
	
	public ResultToCSV(String[] headers, String file_path) throws IOException{
		int length = headers.length;
		header = new String[length];
		for (int i = 0; i < length; i++){
			header[i] = headers[i];
		}
		path = file_path;
		wr = new CsvWriter(path, ',', Charset.forName("utf-8"));
		wr.writeRecord(header);
		wr.flush();
	}
	
	public void write_contents(String[] contents) throws IOException{
		wr.writeRecord(contents);
	}
	
	public void close(){
		wr.close();
	}
}
