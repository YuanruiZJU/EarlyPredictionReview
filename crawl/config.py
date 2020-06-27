import os

download_dir = 'E://GerritDownload/'

# number of code reviews in each step
num_one_step = 100

# REST API URL
urls = {}
urls['eclipse'] = 'https://git.eclipse.org/r/changes/?o=ALL_REVISIONS&o=ALL_FILES&o=ALL_COMMITS&o=MESSAGES&o=DETAILED_ACCOUNTS&n=%s' % num_one_step
urls['aosp'] = 'https://android-review.googlesource.com/changes/?o=ALL_REVISIONS&o=ALL_FILES&o=ALL_COMMITS&o=MESSAGES&o=DETAILED_ACCOUNTS&n=%s' % num_one_step
urls['libreoffice'] = 'https://gerrit.libreoffice.org/changes/?o=ALL_REVISIONS&o=ALL_FILES&o=ALL_COMMITS&o=MESSAGES&o=DETAILED_ACCOUNTS&n=%s' % num_one_step
urls['openstack'] = 'https://review.openstack.org/changes/?o=ALL_REVISIONS&o=ALL_FILES&o=ALL_COMMITS&o=MESSAGES&o=DETAILED_ACCOUNTS&n=%s' % num_one_step
urls['gerrit'] = 'https://review.gerrithub.io/changes/?o=ALL_REVISIONS&o=ALL_FILES&o=ALL_COMMITS&o=MESSAGES&o=DETAILED_ACCOUNTS&n=%s' % num_one_step
urls['qt'] = 'https://codereview.qt-project.org/changes/?o=ALL_REVISIONS&o=ALL_FILES&o=ALL_COMMITS&o=MESSAGES&o=DETAILED_ACCOUNTS&n=%s' % num_one_step


# log path
def log_path(project, status):
    return os.path.join(download_dir, project, 'run' + status + '.log')
