package com.pivotal.pxf.plugins.json;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.fs.Path;
import org.junit.After;
import org.junit.Test;

import com.pivotal.pxf.PxfUnit;
import com.pivotal.pxf.api.Fragmenter;
import com.pivotal.pxf.api.ReadAccessor;
import com.pivotal.pxf.api.ReadResolver;
import com.pivotal.pxf.api.io.DataType;
import com.pivotal.pxf.plugins.hdfs.HdfsDataFragmenter;

public class JsonExtensionTest extends PxfUnit {

	private static List<Pair<String, DataType>> columnDefs = null;
	private static List<Pair<String, String>> extraParams = new ArrayList<Pair<String, String>>();

	static {

		columnDefs = new ArrayList<Pair<String, DataType>>();

		columnDefs.add(new Pair<String, DataType>("created_at", DataType.TEXT));
		columnDefs.add(new Pair<String, DataType>("id", DataType.BIGINT));
		columnDefs.add(new Pair<String, DataType>("text", DataType.TEXT));
		columnDefs.add(new Pair<String, DataType>("user.screen_name",
				DataType.TEXT));
		columnDefs.add(new Pair<String, DataType>("entities.hashtags[0]",
				DataType.TEXT));
		columnDefs.add(new Pair<String, DataType>("coordinates.coordinates[0]",
				DataType.FLOAT8));
		columnDefs.add(new Pair<String, DataType>("coordinates.coordinates[1]",
				DataType.FLOAT8));
	}

	@After
	public void cleanup() throws Exception {
		extraParams.clear();
	}

	@Test
	public void testSmallTweets() throws Exception {

		List<String> output = new ArrayList<String>();

		output.add("Fri Jun 07 22:45:02 +0000 2013,343136547115253761,REPAIR THE TRUST: REMOVE OBAMA/BIDEN FROM OFFICE. #IRS #DOJ #NSA #tcot,SpreadButter,tweetCongress,,");
		output.add("Fri Jun 07 22:45:02 +0000 2013,343136547123646465,@marshafitrie dibagi 1000 aja sha :P,patronusdeadly,,,");
		output.add("Fri Jun 07 22:45:02 +0000 2013,343136547136233472,Vaga: Supervisor de Almoxarifado. Confira em http://t.co/hK5cy5B2oS,NoSecrets_Vagas,,,");
		output.add("Fri Jun 07 22:45:03 +0000 2013,343136551322136576,It's Jun 7, 2013 @ 11pm ; Wind = NNE (30,0) 14.0 knots; Swell = 2.6 ft @ 5 seconds....,SevenStonesBuoy,,-6.1,50.103");

		super.assertOutput(new Path(System.getProperty("user.dir") + "/"
				+ "src/test/resources/tweets-small.json"), output);
	}

	@Test
	public void testTweetsWithNull() throws Exception {

		List<String> output = new ArrayList<String>();

		output.add("Fri Jun 07 22:45:02 +0000 2013,,REPAIR THE TRUST: REMOVE OBAMA/BIDEN FROM OFFICE. #IRS #DOJ #NSA #tcot,SpreadButter,tweetCongress,,");
		output.add("Fri Jun 07 22:45:02 +0000 2013,,@marshafitrie dibagi 1000 aja sha :P,patronusdeadly,,,");

		super.assertOutput(new Path(System.getProperty("user.dir") + "/"
				+ "src/test/resources/null-tweets.json"), output);
	}

	@Test
	public void testSmallTweetsWithDelete() throws Exception {

		List<String> output = new ArrayList<String>();

		output.add(",,,,,,");
		output.add("Fri Jun 07 22:45:02 +0000 2013,343136547115253761,REPAIR THE TRUST: REMOVE OBAMA/BIDEN FROM OFFICE. @GOPoversight @GOPLeader @SenRandPaul @SenTedCruz #tweetCongress #IRS #DOJ #NSA #tcot,SpreadButter,tweetCongress,,");
		output.add("Fri Jun 07 22:45:02 +0000 2013,343136547123646465,@marshafitrie dibagi 1000 aja sha :P,patronusdeadly,,,");
		output.add("Fri Jun 07 22:45:02 +0000 2013,343136547136233472,Vaga: Supervisor de Almoxarifado. Confira em http://t.co/hK5cy5B2oS,NoSecrets_Vagas,,,");

		super.assertOutput(new Path(System.getProperty("user.dir") + "/"
				+ "src/test/resources/tweets-small-with-delete.json"), output);
	}

	@Test
	public void testWellFormedJson() throws Exception {

		extraParams.add(new Pair<String, String>("IDENTIFIER", "record"));
		extraParams.add(new Pair<String, String>("ONERECORDPERLINE", "false"));

		List<String> output = new ArrayList<String>();

		output.add("Fri Jun 07 22:45:02 +0000 2013,343136547115253761,REPAIR THE TRUST: REMOVE OBAMA/BIDEN FROM OFFICE. @GOPoversight @GOPLeader @SenRandPaul @SenTedCruz #tweetCongress #IRS #DOJ #NSA #tcot,SpreadButter,tweetCongress,,");
		output.add("Fri Jun 07 22:45:02 +0000 2013,343136547123646465,@marshafitrie dibagi 1000 aja sha :P,patronusdeadly,,,");
		output.add("Fri Jun 07 22:45:02 +0000 2013,343136547136233472,Vaga: Supervisor de Almoxarifado. Confira em http://t.co/hK5cy5B2oS,NoSecrets_Vagas,,,");

		super.assertOutput(new Path(System.getProperty("user.dir") + "/"
				+ "src/test/resources/tweets-pp.json"), output);
	}

	@Test
	public void testWellFormedJsonWithDelete() throws Exception {

		extraParams.add(new Pair<String, String>("IDENTIFIER", "record"));
		extraParams.add(new Pair<String, String>("ONERECORDPERLINE", "false"));

		List<String> output = new ArrayList<String>();

		output.add(",,,,,,");
		output.add("Fri Jun 07 22:45:02 +0000 2013,343136547115253761,REPAIR THE TRUST: REMOVE OBAMA/BIDEN FROM OFFICE. @GOPoversight @GOPLeader @SenRandPaul @SenTedCruz #tweetCongress #IRS #DOJ #NSA #tcot,SpreadButter,tweetCongress,,");
		output.add("Fri Jun 07 22:45:02 +0000 2013,343136547123646465,@marshafitrie dibagi 1000 aja sha :P,patronusdeadly,,,");
		output.add("Fri Jun 07 22:45:02 +0000 2013,343136547136233472,Vaga: Supervisor de Almoxarifado. Confira em http://t.co/hK5cy5B2oS,NoSecrets_Vagas,,,");

		super.assertOutput(new Path(System.getProperty("user.dir") + "/"
				+ "src/test/resources/tweets-pp-with-delete.json"), output);
	}

	@Test
	public void testMultipleFiles() throws Exception {

		List<String> output = new ArrayList<String>();

		output.add("Fri Jun 07 22:45:02 +0000 2013,343136547115253761,REPAIR THE TRUST: REMOVE OBAMA/BIDEN FROM OFFICE. #IRS #DOJ #NSA #tcot,SpreadButter,tweetCongress,,");
		output.add("Fri Jun 07 22:45:02 +0000 2013,343136547123646465,@marshafitrie dibagi 1000 aja sha :P,patronusdeadly,,,");
		output.add("Fri Jun 07 22:45:02 +0000 2013,343136547136233472,Vaga: Supervisor de Almoxarifado. Confira em http://t.co/hK5cy5B2oS,NoSecrets_Vagas,,,");
		output.add("Fri Jun 07 22:45:03 +0000 2013,343136551322136576,It's Jun 7, 2013 @ 11pm ; Wind = NNE (30,0) 14.0 knots; Swell = 2.6 ft @ 5 seconds....,SevenStonesBuoy,,-6.1,50.103");
		output.add(",,,,,,");
		output.add("Fri Jun 07 22:45:02 +0000 2013,343136547115253761,REPAIR THE TRUST: REMOVE OBAMA/BIDEN FROM OFFICE. @GOPoversight @GOPLeader @SenRandPaul @SenTedCruz #tweetCongress #IRS #DOJ #NSA #tcot,SpreadButter,tweetCongress,,");
		output.add("Fri Jun 07 22:45:02 +0000 2013,343136547123646465,@marshafitrie dibagi 1000 aja sha :P,patronusdeadly,,,");
		output.add("Fri Jun 07 22:45:02 +0000 2013,343136547136233472,Vaga: Supervisor de Almoxarifado. Confira em http://t.co/hK5cy5B2oS,NoSecrets_Vagas,,,");

		super.assertUnorderedOutput(new Path(System.getProperty("user.dir")
				+ "/" + "src/test/resources/tweets-small*.json"), output);
	}

	@Override
	public List<Pair<String, String>> getExtraParams() {
		return extraParams;
	}

	@Override
	public Class<? extends Fragmenter> getFragmenterClass() {
		return HdfsDataFragmenter.class;
	}

	@Override
	public Class<? extends ReadAccessor> getReadAccessorClass() {
		return JsonAccessor.class;
	}

	@Override
	public Class<? extends ReadResolver> getReadResolverClass() {
		return JsonResolver.class;
	}

	@Override
	public List<Pair<String, DataType>> getColumnDefinitions() {
		return columnDefs;
	}
}
