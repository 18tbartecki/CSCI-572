<?php

// make sure browsers see this page as utf-8 encoded HTML
header('Content-Type: text/html; charset=utf-8');

$limit = 10;
$query = isset($_REQUEST['q']) ? $_REQUEST['q'] : false;
$results = false;

if ($query)
{
  // The Apache Solr Client library should be on the include path
  // which is usually most easily accomplished by placing in the
  // same directory as this script ( . or current directory is a default
  // php include path entry in the php.ini)
  require_once('Apache/Solr/Service.php');

  // create a new solr service instance - host, port, and webapp
  // path (all defaults in this example)
  $solr = new Apache_Solr_Service('localhost', 8983, '/solr/hw');

  // if magic quotes is enabled then stripslashes will be needed
  // if (get_magic_quotes_gpc() == 1)
  // {
  //   $query = stripslashes($query);
  // }

  // in production code you'll always want to use a try /catch for any
  // possible exceptions emitted  by searching (i.e. connection
  // problems or a query parsing error)
  try
  {
  	if(!isset($_GET['search'])) {
  		$_GET['search'] = "lucene";
  	}

	if($_GET['search'] == 'lucene') {
		
		$results = $solr->search($query, 0, $limit);
	}
	else {
		$params = array(
			'sort'=>'pageRankFile.txt desc', 
			'fq'=>'a filtering query',
			'facet'=>'true',
			);
		$results = $solr->search($query, 0, $limit, $params);
	}
  }
  catch (Exception $e)
  {
    // in production you'd probably log or email this error to an admin
    // and then show a special message to the user but for this example
    // we're going to show the full exception
    die("<html><head><title>SEARCH EXCEPTION</title><body><pre>{$e->__toString()}</pre></body></html>");
  }
}


?>
<html>
  <head>
    <title>PHP Solr Client Example</title>
  </head>
  <body>
    <form  accept-charset="utf-8" method="get">
      <label for="q">Search:</label>
      <input id="q" name="q" type="text" value="<?php echo htmlspecialchars($query, ENT_QUOTES, 'utf-8'); ?>"/>
      <input type="submit"/>
      <input type="radio" name="search" <?php if (isset($_GET['search']) && $_GET['search']=="lucene") echo 'checked="checked"';?>  value="lucene" /> Lucene
	<input type="radio" name="search" <?php if (isset($_GET['search']) && $_GET['search']=="pagerank") echo 'checked="checked"';?> value="pagerank" /> PageRank <br/><br/> 
	
    </form>
<?php

// display results
if ($results)
{
  $total = (int) $results->response->numFound;
  $start = min(1, $total);
  $end = min($limit, $total);
?>
    <div>Results <?php echo $start; ?> - <?php echo $end;?> of <?php echo $total; ?>:</div>
    
<?php
  // iterate result documents
  foreach ($results->response->docs as $doc)
  {

    // iterate document fields / values
    foreach ($doc as $field => $value)
    {
    	if($field == "_version_") continue;
    	if($field == "title") $title = $value;
    	if($field == "og_description") $description = $value;
    	if($field == "og_url") $url = $value;
    	if($field == "id") $path = $value;
    }
    	echo "<hr>";
    	echo "</br>";
    	echo "<a href='$url'>$title</a></br>";
    	echo "<a href='$url'>$url</a></br>";
    	echo "ID: $path </br>";
    	echo "Description: $description</br>";
    	echo "<br>";
    	
  }

}

	
?>
  </body>
</html>

