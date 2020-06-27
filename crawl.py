import simplejson
import requests
import os
import threading
import config

# projects
projects = ['eclipse', 'libreoffice', 'openstack', 'qt']
# code review state
statuses = ['merged', 'abandoned']

# download dir
download_dir = config.download_dir
# RestAPI Url
urls = config.urls
# number of code reviews downloaded in each step
num_one_step = config.num_one_step


# retrieve all code reviews
def get_changes(project, start_index, status):
    if project in urls.keys():
        url = urls[project]
        url = url + '&q=status:%s&S=%s' % (status, start_index)
        print(url)
        # Here, we get the byte data, we need to transfer it to string
        return requests.get(url).content[4:].decode('utf-8')
    return ''


# Code review store path for each project
def project_dir(project, status):
    dir_path = os.path.join(download_dir, project)
    if not os.path.exists(dir_path):
        os.makedirs(dir_path)
    status_dir_path = os.path.join(dir_path, status)
    if not os.path.exists(status_dir_path):
        os.makedirs(status_dir_path)


# make dir for each project
def mkdir_for_projects():
    for p in projects:
        for s in statuses:
            project_dir(p, s)


# remove all logs
def remove_all_logs():
    for p in projects:
        for s in statuses:
            path = p + '-' + s + '.log'
            if(os.path.exists(path)):
                os.remove(path)


def write_file(file_path, message):
    file_obj = open(file_path, 'w', encoding='utf-8')
    file_obj.write(message)
    file_obj.close()


# download threads
def download(project, status, start_index=0, one_step=100):
    print(project + ": " + status + " download")
    has_more = True
    dir_path = os.path.join(download_dir, project, status)

    # Log file initialization
    logfile_name = config.log_path(project, status)
    log_obj = open(logfile_name, 'a+')
    log_obj.write('start log\n')

    try_num = 3
    while has_more:
        file_name = '%s-%s.json' % (start_index, start_index + num_one_step - 1)
        file_path = os.path.join(dir_path, file_name)
        try:
            if os.path.exists(file_path):
                start_index = start_index + one_step
                continue

            changes_str = get_changes(project, start_index, status)
        except:
            if try_num > 0:
                try_num = try_num - 1
            else:
                try_num = 3
                log_message = '%s %s %s to %s exception!' % (
                project, status, start_index, start_index + num_one_step - 1)
                print(log_message)
                log_obj.write(log_message + '\n')
                start_index = start_index + one_step
            pass
        change_dict_list = simplejson.loads(changes_str)
        if len(change_dict_list) == 0:
            break

        # length less than number of step, indicate the code review download complete
        if len(change_dict_list) < num_one_step:
            break

        write_file(file_path, changes_str)
        log_message = '%s %s %s to %s has downloaded!' % (project, status, start_index, start_index+num_one_step-1)
        log_obj.write(log_message+'\n')
        print(log_message)
        start_index = start_index + one_step

    log_obj.write('end log\n')
    print(project + " end")
    log_obj.close()


if __name__ == '__main__':
    mkdir_for_projects()
    remove_all_logs()

    # create thread for each project and each state
    for p in projects:
        for s in statuses:
            t = threading.Thread(target=download, args=(p, s, 0, 100,))
            t.start()
