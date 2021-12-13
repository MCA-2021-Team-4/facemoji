import os

target_platforms = ["Apple", "Facebook", "Google", "Samsung", "Twitter"]
cwd = os.getcwd()

for platform in target_platforms:
    os.chdir(os.path.join(cwd, "emojis/{}".format(platform)))
    for i, file in enumerate(sorted(os.listdir("."))):
        os.system("mv {} {}".format(file ,file[:file.find("_")] + ".png"))
