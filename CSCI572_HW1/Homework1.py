import json
from bs4 import BeautifulSoup
import time
from time import sleep
import requests
from random import randint
from html.parser import HTMLParser

USER_AGENT = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) '
                  'Chrome/61.0.3163.100 Safari/537.36'}


class SearchEngine:
    @staticmethod
    def search(query, sleep=True):
        if sleep:  # Prevents loading too many pages too soon
            time.sleep(randint(5, 10))
        temp_url = '+'.join(query.split())  # for adding + between words for the query
        url = 'http://www.ask.com/web?q=' + temp_url
        soup = BeautifulSoup(requests.get(url, headers=USER_AGENT).text, "html.parser")
        new_results = SearchEngine.scrape_search_result(soup, temp_url, 1)
        return new_results

    @staticmethod
    def scrape_search_result(soup, temp_url, page):
        raw_results = soup.find_all("div", attrs={"class": "PartialSearchResults-item-title"})
        results = []
        # implement a check to get only 10 results and also check that URLs must not be duplicated
        if len(raw_results) < 10 and page != 2:
            new_url = "http://www.ask.com/web?q=" + temp_url + "&page=2"
            print(new_url)
            soup = BeautifulSoup(requests.get(new_url, headers=USER_AGENT).text, "html.parser")
            page_2_results = SearchEngine.scrape_search_result(soup, temp_url, 2)

        for result in raw_results:
            link = result.find('a').get('href')
            results.append(link)

        if len(results) < 10 and page != 2 and page_2_results:
            results.append(page_2_results)
        return results


def read_file(file_name):
    fileIn = open(file_name, "r")
    if file_name == "100QueriesSet3.txt":
        final_list = []
        # Remove extra space and add to list of genre matches
        for line in fileIn:
            line = line.strip()
            final_list.append(line)
        fileIn.close()
        return final_list
    else:
        json_data = json.load(fileIn)
        return json_data


def write_file(json_object):
    with open("hw5.json", "w") as fileOut:
        fileOut.write(json_object)


def remove_http(url):
    if "https" in url:
        no_http_url = url[5:]
    elif "http" in url:
        no_http_url = url[4:]
    return no_http_url


def remove_end_slash(url):
    if url[len(url) - 1] == '/':
        compare = url[:len(url) - 1]
    else:
        compare = url
    return compare


def main():
    query_list = read_file("100QueriesSet3.txt")
    google_results = read_file("Google_Result3.json")

    data = {}
    for query in query_list:
        results = SearchEngine.search(query)
        data[query] = results
        # To test if 10 results returned
        print(len(data[query]))

    json_data = json.dumps(data, indent=4)
    write_file(json_data)
    print(json_data)

    correlations = []
    spearmans = {}
    spearmans_final_values = []

    for query in query_list:
        spearmans[query] = []
        correlation = 0
        ask_index = 1
        for url in data[query]:
            # Remove HTTPS and HTTP to compare
            no_http_url = remove_http(url)
            # Remove / at the end of urls
            compare = remove_end_slash(no_http_url)
            google_index = 1

            for google_url in google_results[query]:
                no_http_google_url = remove_http(google_url)
                google_url_compare = remove_end_slash(no_http_google_url)

                if compare.lower() == google_url_compare.lower():
                    correlation += 1
                    if query not in spearmans:
                        spearmans[query] = (ask_index, google_index)
                    else:
                        spearmans[query].append((ask_index, google_index))
                    print(url)
                    break
                google_index += 1
            ask_index += 1

        correlations.append(correlation)
        print(correlation)
        print(spearmans)

    index = 0
    for query in query_list:
        total_d = 0
        n = correlations[index]
        index += 1

        if n == 0:
            rho = 0
        elif n == 1:
            for pairing in spearmans[query]:
                if pairing[0] == pairing[1]:
                    rho = 1
                else:
                    rho = 0
        else:
            for pairing in spearmans[query]:
                d_squared = (pairing[1] - pairing[0]) ** 2
                total_d += d_squared
            rho = 1 - (6*total_d)/(n*((n**2) - 1))

        spearmans_final_values.append(rho)

    with open("hw5.csv", "w") as outputDataFile:
        heading = "Queries, Number of Overlapping Results, Percent Overlap, Spearman Coefficient\n"
        outputDataFile.write(heading)
        overlaps = 0
        overall_percentage = 0
        overall_spearman = 0

        for i in range(100):
            line = "Query " + str((i + 1)) + ", " + str(correlations[i]) + ", " + str(float(correlations[i]*10)) + ", "\
                   + str("{:.2f}".format(spearmans_final_values[i])) + "\n"
            outputDataFile.write(line)
            overlaps += correlations[i]
            overall_percentage += float(correlations[i]*10)
            overall_spearman += spearmans_final_values[i]

        summary = "Averages, " + str(overlaps/100) + ", " + str(overall_percentage/100) + ", " + \
                  str("{:.2f}".format(overall_spearman/100))
        outputDataFile.write(summary)


main()
